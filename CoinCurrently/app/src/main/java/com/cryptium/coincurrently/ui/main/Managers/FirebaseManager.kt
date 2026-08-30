@file:Suppress("SameReturnValue")

package com.cryptium.coincurrently.ui.main.Managers

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cryptium.coincurrently.ui.main.General.AppColorScheme
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.General.PreferredTimeframe
import com.google.android.gms.tasks.Tasks
import com.cryptium.coincurrently.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.Repositories.StoreKitManager
import com.google.gson.Gson
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import networking.ResponseModels.CurrencySelectionModel
import kotlin.coroutines.resume

@Suppress("SameReturnValue")
class FirebaseManager {
    companion object {

        private const val MAX_NETWORK_RETRIES = 3
        private const val RETRY_DELAY_MS = 2000L
        private const val GET_ID_TOKEN_TIMEOUT_MS = 15_000L

        private lateinit var auth: FirebaseAuth
        private var uid: String? = null
        var showDetailsNews = true
        var user: FirebaseUser? = null
        var idToken: String = ""
        var showPromotionalOffer = false

        var fcmToken = ""

        @Volatile
        var isRemoteConfigReady: Boolean = false
            private set

        private val remoteConfigMutex = Mutex()

        private fun applyRemoteConfigValues(firebaseRemoteConfig: com.google.firebase.remoteconfig.FirebaseRemoteConfig) {
            showDetailsNews = firebaseRemoteConfig.getBoolean(RemoteConfigKeys.ShowDetailNews.key)
            showPromotionalOffer = firebaseRemoteConfig.getBoolean(RemoteConfigKeys.ShowPromotionalOffer.key)
            isRemoteConfigReady = true
        }

        /** Fetches and activates remote config if it has not been loaded yet (mirrors iOS getConfig). */
        suspend fun ensureRemoteConfigFetched() {
            if (isRemoteConfigReady) return
            remoteConfigMutex.withLock {
                if (isRemoteConfigReady) return
                val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
                suspendCancellableCoroutine { continuation ->
                    firebaseRemoteConfig.fetch(0).addOnCompleteListener { fetchTask ->
                        if (!fetchTask.isSuccessful) {
                            isRemoteConfigReady = true
                            continuation.resume(Unit)
                            return@addOnCompleteListener
                        }
                        firebaseRemoteConfig.activate().addOnCompleteListener {
                            applyRemoteConfigValues(firebaseRemoteConfig)
                            continuation.resume(Unit)
                        }
                    }
                }
            }
        }

        suspend fun getConfigBool(key: RemoteConfigKeys): Boolean? {
            ensureRemoteConfigFetched()
            return FirebaseRemoteConfig.getInstance().getBoolean(key.key)
        }

        suspend fun getConfigString(key: RemoteConfigKeys): String? {
            ensureRemoteConfigFetched()
            return FirebaseRemoteConfig.getInstance().getString(key.key).takeIf { it.isNotBlank() }
        }

        suspend fun <T> getConfigJsonValue(key: RemoteConfigKeys, clazz: Class<T>): T? {
            ensureRemoteConfigFetched()
            return try {
                val json = FirebaseRemoteConfig.getInstance().getString(key.key)
                if (json.isBlank()) {
                    null
                } else {
                    Gson().fromJson(json, clazz)
                }
            } catch (_: Exception) {
                null
            }
        }

        suspend fun getHiddenSecret(): HiddenSecret? =
            getConfigJsonValue(RemoteConfigKeys.ShowSecretMessage, HiddenSecret::class.java)

        /**
         * FirebaseNetworkException often happens even when the device "has connection" because of:
         * - Captive portals (WiFi that requires login), slow or flaky networks, timeouts
         * - VPNs/firewalls blocking Google services, or DNS/connectivity not ready at app start
         * We retry a few times before giving up.
         */
        private fun isNetworkError(exception: Exception?) =
            exception is FirebaseNetworkException ||
                (exception?.cause as? FirebaseNetworkException) != null

        private fun scheduleRetry(
            activity: Activity,
            context: Context,
            attempt: Int,
            callback: (forceUpdate: Boolean) -> Unit
        ) {
            Handler(Looper.getMainLooper()).postDelayed({
                attemptSetup(activity, context, attempt, callback)
            }, RETRY_DELAY_MS)
        }

        private fun attemptSetup(
            activity: Activity,
            context: Context,
            attempt: Int,
            callback: (forceUpdate: Boolean) -> Unit
        ) {
            auth.signInAnonymously()
                .addOnCompleteListener(activity) { task ->
                    if (!task.isSuccessful) {
                        if (isNetworkError(task.exception) && attempt < MAX_NETWORK_RETRIES) {
                            scheduleRetry(activity, context, attempt + 1, callback)
                        } else {
                            callback(false)
                        }
                        return@addOnCompleteListener
                    }
                    uid = task.result?.user?.uid
                    user = task.result?.user
                    if (user == null) {
                        callback(false)
                        return@addOnCompleteListener
                    }

                    syncPurchases()

                    val handler = Handler(Looper.getMainLooper())
                    var idTokenTimedOut = false
                    val idTokenTimeoutRunnable = Runnable {
                        if (idTokenTimedOut) return@Runnable
                        idTokenTimedOut = true
                        if (attempt < MAX_NETWORK_RETRIES) {
                            scheduleRetry(activity, context, attempt + 1, callback)
                        } else {
                            callback(false)
                        }
                    }
                    handler.postDelayed(idTokenTimeoutRunnable, GET_ID_TOKEN_TIMEOUT_MS)

                    user?.getIdToken(false)?.addOnCompleteListener(activity) { tokenTask ->
                        if (idTokenTimedOut) return@addOnCompleteListener
                        handler.removeCallbacks(idTokenTimeoutRunnable)

                        if (!tokenTask.isSuccessful) {
                            if (isNetworkError(tokenTask.exception) && attempt < MAX_NETWORK_RETRIES) {
                                scheduleRetry(activity, context, attempt + 1, callback)
                            } else {
                                callback(false)
                            }
                            return@addOnCompleteListener
                        }
                        idToken = tokenTask.result?.token ?: ""

                        FirebaseMessaging.getInstance().token.addOnCompleteListener(activity) { fcmTask ->
                            if (!fcmTask.isSuccessful) {
                                if (isNetworkError(fcmTask.exception) && attempt < MAX_NETWORK_RETRIES) {
                                    scheduleRetry(activity, context, attempt + 1, callback)
                                } else {
                                    callback(false)
                                }
                                return@addOnCompleteListener
                            }
                            fcmToken = fcmTask.result.toString()

                            val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
                            firebaseRemoteConfig.fetch(0).addOnCompleteListener(activity) { fetchTask ->
                                if (!fetchTask.isSuccessful) {
                                    if (isNetworkError(fetchTask.exception) && attempt < MAX_NETWORK_RETRIES) {
                                        scheduleRetry(activity, context, attempt + 1, callback)
                                    } else {
                                        callback(false)
                                    }
                                    return@addOnCompleteListener
                                }
                                firebaseRemoteConfig.activate()
                                    .addOnCompleteListener(activity) { _ ->
                                        applyRemoteConfigValues(firebaseRemoteConfig)

                                        val mustUpdate = mustForceUpdate(
                                            minimumVersion = firebaseRemoteConfig.getString(
                                                RemoteConfigKeys.MinimumVersionAndroid.key,
                                            ),
                                        )

                                        callback(mustUpdate)
                                    }
                            }
                        }
                    }
                }
        }

        fun syncPurchases() {
            val appUserId = uid ?: FirebaseAuth.getInstance().currentUser?.uid

            fun fetchAndApplyCurrentCustomerInfo() {
                Purchases.sharedInstance.getCustomerInfo(
                    CacheFetchPolicy.FETCH_CURRENT,
                    object : ReceiveCustomerInfoCallback {
                        override fun onReceived(customerInfo: CustomerInfo) {
                            Handler(Looper.getMainLooper()).post {
                                StoreKitManager.applyCustomerInfo(customerInfo)
                            }
                        }

                        override fun onError(error: PurchasesError) {
                            // Cached pro state remains until next sync.
                        }
                    },
                )
            }

            if (appUserId.isNullOrEmpty()) {
                fetchAndApplyCurrentCustomerInfo()
                return
            }

            Purchases.sharedInstance.logIn(
                appUserId,
                object : LogInCallback {
                    override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                        fetchAndApplyCurrentCustomerInfo()
                    }

                    override fun onError(error: PurchasesError) {
                        fetchAndApplyCurrentCustomerInfo()
                    }
                },
            )
        }

        fun setupFirebase(
            activity: Activity,
            context: Context,
            callback: (forceUpdate: Boolean) -> Unit
        ) {
            try {
                auth = FirebaseAuth.getInstance()
                FirebaseApp.initializeApp(context)

                AnalyticsManager.init(context)

                val firebaseAppCheck = FirebaseAppCheck.getInstance()
                firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())

                attemptSetup(activity, context, 0, callback)
            } catch (_: Exception) {
                callback(false)
            }
        }

        fun getFirebaseToken(): String {
            return try {
                auth = FirebaseAuth.getInstance()
                val signInTask = auth.signInAnonymously()

                Tasks.await(signInTask)

                val user = signInTask.result.user
                if (user != null) {
                    val tokenTask = user.getIdToken(false)
                    Tasks.await(tokenTask)
                    tokenTask.result?.token ?: ""
                } else {
                    ""
                }
            } catch (_: Exception) {
                ""
            }
        }

        fun getHiddenSecretSync(): HiddenSecret? {
            if (!isRemoteConfigReady) return null
            return try {
                val json = FirebaseRemoteConfig.getInstance().getString(RemoteConfigKeys.ShowSecretMessage.key)
                if (json.isBlank()) {
                    null
                } else {
                    Gson().fromJson(json, HiddenSecret::class.java)
                }
            } catch (_: Exception) {
                null
            }
        }

        fun fetchFcmTokenIfNeeded(): Boolean {
            if (fcmToken.isNotEmpty()) {
                return true
            }

            return try {
                val tokenTask = FirebaseMessaging.getInstance().token
                Tasks.await(tokenTask)

                if (tokenTask.isSuccessful) {
                    fcmToken = tokenTask.result.orEmpty()
                    fcmToken.isNotEmpty()
                } else {
                    false
                }
            } catch (_: Exception) {
                false
            }
        }

        private fun padVersion(version: String): List<Int> {
            val components = version.split(".").map { it.toIntOrNull() ?: 0 }
            val needed = maxOf(3 - components.size, 0)
            return components + List(needed) { 0 }
        }

        private fun compareVersions(v1: String, v2: String): Int {
            val paddedV1 = padVersion(v1)
            val paddedV2 = padVersion(v2)
            for (i in paddedV1.indices) {
                when {
                    paddedV1[i] < paddedV2[i] -> return -1
                    paddedV1[i] > paddedV2[i] -> return 1
                }
            }
            return 0
        }

        private fun mustForceUpdate(minimumVersion: String): Boolean {
            if (minimumVersion.isBlank()) return false
            return try {
                compareVersions(BuildConfig.VERSION_NAME, minimumVersion) < 0
            } catch (e: Exception) {
                Log.d(e.localizedMessage, "Couldn't compare versions")
                false
            }
        }

        fun resetApp(callback: () -> Unit) {
            val db = FirebaseFirestore.getInstance()

            GlobalFunctions.setPortfolioCurrency(arrayListOf())
            GlobalFunctions.setWidgetCurrency(listOf())
            GlobalFunctions.setSelectedCurrency(CurrencySelectionModel())
            GlobalFunctions.setFavorites(listOf())
            StorageManager.set(StorageKeys.APP_COLOR_SCHEME, AppColorScheme.DARK)
            GlobalFunctions.setIsWidgetDarkModeEnabled(true)
            GlobalFunctions.setIsBiometricAuthenticationEnabled(false)
            GlobalFunctions.setIsHidePortfolioEnabled(false)
            GlobalFunctions.setShowChangesInPercent(true)
            GlobalFunctions.setPreferredTimeframe(PreferredTimeframe.WEEKLY)
            StorageManager.clearCache()

            db.collection(uid.toString())
                .get()
                .addOnSuccessListener {
                    for (document in it.documents) {
                        document.reference.delete()
                    }
                }


            val collection = GlobalFunctions.getLastLoginCollection()

            db.collection("last_login${collection}")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener {
                    for (document in it.documents) {
                        document.reference.delete()
                    }
                }

            FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener {
                callback.invoke()
            }
        }
    }
}

