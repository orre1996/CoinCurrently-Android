package com.cryptium.coincurrently.core.networking
import android.content.Context
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.extensions.isMoreThanDefaultCache
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.BuildConfig
import com.cryptium.coincurrently.core.networking.ResponseModels.ApiService
import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryModel
import com.cryptium.coincurrently.core.networking.ResponseModels.serverBaseUrl
import com.google.gson.Gson
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Timeout
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.ArrayList
import java.util.Date
import java.util.List
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

 class MyOkHttpInterceptor(val platform: String, val context: Context): Interceptor {
     override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()
        if (!originalRequest.url.toString().contains(serverBaseUrl) ) {
            return chain.proceed(originalRequest)
        }

        val token = FirebaseManager.getFirebaseToken()

        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .header("x-source", "Android-$platform-${NavigationViewModel.currentScreen}")
            .header("x-coin-currently-key", BuildConfig.BACKEND_API_KEY)
            .removeHeader("Pragma")

        val schedulerSecret = BuildConfig.SCHEDULER_PRICE_ALERT_SECRET
        if (schedulerSecret.isNotEmpty() && (
                    originalRequest.url.toString().contains("price-alert-trigger") ||
                    originalRequest.url.toString().contains("big-mover-trigger")
                )) {
            newRequest.header("x-scheduler-price-alert-secret", schedulerSecret)
        }

        val builtRequest = newRequest
            .build()

        if (token.isEmpty()) {
            MainActivity.showSnackbar(
                context,
                SnackBarType.FAILURE,
                NetworkError.CouldntGenerateIdToken.errorMessage(null, context)
            )
        }

        return chain.proceed(builtRequest)
    }
}


@Suppress("UNCHECKED_CAST")
private fun <T> Any?.asCachedData(): CachedData<T>? = this as? CachedData<T>

object RetrofitBuilder {
    private const val BASE_URL = "http://localhost/"

    /** URL constants for cache lookups in repository init. Must match actual request URLs. */
    const val FEAR_GREED_URL = "https://api.alternative.me/fng/?limit=0&format=json"

    private val responseCache = ConcurrentHashMap<String, CachedData<*>>()
    private lateinit var okHttpClient: OkHttpClient

    /** Max age in minutes for specific endpoints. Null = use default (3 min). 0 = never serve cached. */
    private fun getCacheMaxAgeMinutes(url: String): Int? = when {
        url.contains("coins/list") && !url.contains("list/new") -> 60 * 24 * 1   // 1 day - allCoins
        url.contains("/api/v3/exchanges?") -> 60 * 24 * 1   // 1 day - allExchanges
        url.contains("asset_platforms") && url.contains("filter=nft") -> 60 * 24 * 1   // 1 day - nftPlatforms
        url.contains("entities/list") -> 60 * 24 * 1   // 1 day - entitiesList
        url.contains("search/trending") -> 60   // 1 hour - trending
        url.contains("/api/v3/search?") -> 60   // 1 hour - search
        url.contains("/get-alerts") -> 0   // no cache - getAlerts
        else -> null
    }

    private fun isCacheExpired(cached: CachedData<*>, url: String): Boolean {
        val maxAgeMinutes = getCacheMaxAgeMinutes(url)
        return if (maxAgeMinutes != null) {
            (Date().time - cached.date.time) > maxAgeMinutes * 60 * 1000L
        } else {
            cached.date.isMoreThanDefaultCache()
        }
    }


    fun <T> getCached(url: String, includeExpired: Boolean = false): CachedData<T>? {
        var cached = responseCache[url].asCachedData<T>()
        if (cached == null) {
            try {
                cached = StorageManager.getUrlCache<CachedData<T>>(url)
                cached?.let { responseCache[url] = it }
            } catch (_: Exception) { /* cache not ready */ }
        }
        if (cached == null) return null
        if (!includeExpired && isCacheExpired(cached, url)) return null
        var data: Any? = cached.data
        while (data is Result<*>) {
            data = data.getOrNull()
        }
        if (data == null) return null
        if (isCorruptCachedPayload(data)) {
            removeCached(url)
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return CachedData(data as T, cached.date)
    }

    /** Overload with explicit Type for proper Gson deserialization (avoids LinkedTreeMap cast for array types). */
    fun getCached(url: String, dataType: Type, includeExpired: Boolean = false): CachedData<*>? {
        var cached = responseCache[url].asCachedData<Any>()
        if (cached == null) {
            try {
                val storageType = when {
                    dataType is ParameterizedType -> {
                        val rawType = dataType.rawType
                        val elemType = dataType.actualTypeArguments.getOrNull(0)
                        if ((rawType == List::class.java || rawType == ArrayList::class.java) && elemType != null) {
                            TypeToken.getParameterized(
                                CachedData::class.java,
                                TypeToken.getParameterized(ArrayList::class.java, elemType).type
                            ).type
                        } else TypeToken.getParameterized(CachedData::class.java, dataType).type
                    }
                    else -> TypeToken.getParameterized(CachedData::class.java, dataType).type
                }
                cached = StorageManager.getUrlCache(url, storageType).asCachedData<Any>()
                cached?.let { responseCache[url] = it }
            } catch (_: Exception) { /* cache not ready */ }
        }
        if (cached == null) return null
        if (!includeExpired && isCacheExpired(cached, url)) return null
        var data: Any? = cached.data
        while (data is Result<*>) {
            data = data.getOrNull()
        }
        if (data == null || isCorruptCachedPayload(data)) {
            if (data != null) removeCached(url)
            return null
        }
        return CachedData(data, cached.date)
    }

    /** Uses explicit TypeToken for proper Gson deserialization of array types (avoids LinkedTreeMap cast). */
    internal inline fun <reified E> getCachedList(url: String, includeExpired: Boolean = false): CachedData<ArrayList<E>>? {
        var cached = responseCache[url].asCachedData<ArrayList<E>>()
        if (cached == null) {
            try {
                val type = TypeToken.getParameterized(
                    CachedData::class.java,
                    TypeToken.getParameterized(ArrayList::class.java, E::class.java).type
                ).type
                cached = StorageManager.getUrlCache(url, type).asCachedData<ArrayList<E>>()
                cached?.let { responseCache[url] = it }
            } catch (_: Exception) { /* cache not ready */ }
        }
        if (cached == null) return null
        if (!includeExpired && isCacheExpired(cached, url)) return null
        var data: Any? = cached.data
        while (data is Result<*>) {
            data = data.getOrNull()
        }
        if (data == null) return null
        if (isCorruptCachedPayload(data)) {
            removeCached(url)
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return CachedData(data as ArrayList<E>, cached.date)
    }

    fun clearResponseCache() {
        responseCache.clear()
    }

    fun removeCached(url: String) {
        responseCache.remove(url)
        StorageManager.removeUrlCache(url)
    }

    internal fun setCached(url: String, data: CachedData<*>) {
        responseCache[url] = data
        StorageManager.setUrlCache(url, data)
    }

    /** For repositories that merge/update cached data (e.g. updateFromToplist). */
    fun updateCache(url: String, data: CachedData<*>) = setCached(url, data)


    /**
     * Skip caching payloads that are known-bad after R8/Gson failures:
     * raw Maps (LinkedTreeMap), or wrapper models whose nested `data` is null.
     */
    internal fun shouldCacheSuccessBody(data: Any): Boolean {
        if (data is Map<*, *>) return false
        if (data is com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketModel && data.data == null) {
            return false
        }
        if (data is com.cryptium.coincurrently.core.networking.ResponseModels.FearGreed && data.data == null) {
            return false
        }
        return true
    }

    internal fun isCorruptCachedPayload(data: Any?): Boolean {
        if (data == null) return true
        if (data is Map<*, *>) return true
        if (data is List<*>) {
            val first = data.firstOrNull() ?: return false
            return first is Map<*, *>
        }
        return false
    }

    /**
     * Returns stored response for a GET URL when callers need a fallback after an error
     * (includes expired app cache backing the Retrofit/OkHttp pipeline).
     */
    internal fun <T> getStaleGetCache(url: String, responseType: Type?): CachedData<T>? {
        @Suppress("UNCHECKED_CAST")
        val cached: CachedData<*>? = responseType?.let { type ->
            getCached(url, type, includeExpired = true)
        } ?: getCached<T>(url, includeExpired = true)
        if (cached == null) return null
        var data: Any? = cached.data
        while (data is Result<*>) {
            data = data.getOrNull()
        }
        if (data == null) return null
        @Suppress("UNCHECKED_CAST")
        return CachedData(data as T, cached.date)
    }

    private fun getRetrofit(context: Context, platform: String): Retrofit {
        val httpCacheDirectory = File(context.getActivity()?.cacheDir, "http-cache")
        val cacheSize: Long = 10 * 1024 * 1024 // 10 MiB
        val cache = Cache(httpCacheDirectory, cacheSize)

        val httpClient = OkHttpClient
            .Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(MyOkHttpInterceptor(platform, context))
            .cache(cache)
            .addNetworkInterceptor(REWRITE_RESPONSE_INTERCEPTOR)
            .addInterceptor(REWRITE_RESPONSE_INTERCEPTOR_OFFLINE(context))
            .build()

        okHttpClient = httpClient

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addCallAdapterFactory(ResultCallAdapterFactory(context))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun createApiService(context: Context, platform: String) {
        apiService = getRetrofit(context, platform).create(ApiService::class.java)
    }

    fun ensureApiService(context: Context, platform: String) {
        if (!this::apiService.isInitialized) {
            createApiService(context, platform)
        }
    }

    lateinit var apiService: ApiService
}

class ResultCallAdapterFactory(val context: Context) : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {

        if (getRawType(returnType) != Call::class.java || returnType !is ParameterizedType) {
            return null
        }

        val upperBound = getParameterUpperBound(0, returnType)

        // Use getRawType so R8-obfuscated / wildcard Result types still match.
        // If this factory does not match, Gson deserializes as kotlin.Result (object)
        // and list APIs that return JSON arrays crash with BEGIN_OBJECT vs BEGIN_ARRAY.
        if (getRawType(upperBound) != Result::class.java) {
            return null
        }

        val responseType = if (upperBound is ParameterizedType) {
            getParameterUpperBound(0, upperBound)
        } else {
            Any::class.java
        }

        return object : CallAdapter<Any, Call<Result<Any>>> {
            override fun responseType(): Type = responseType

            override fun adapt(call: Call<Any>): Call<Result<Any>> =
                ResultCall(call, context, responseType)
        }
    }
}

class ResultCall<T>(private val delegate: Call<T>, val context: Context, private val responseType: Type? = null) : Call<Result<T>> {
    private fun successValue(response: Response<T>): T {
        if (responseType is ParameterizedType && responseType.rawType == Response::class.java) {
            @Suppress("UNCHECKED_CAST")
            return response as T
        }

        return response.body()!!
    }

    private fun readErrorBody(response: Response<T>): String? {
        return try {
            response.errorBody()?.string()
        } catch (_: Exception) {
            null
        }
    }

    private fun emptyBodyIfNotFound(url: String, error: NetworkError): T? {
        if (!NetworkError.isPublicTreasuryListUrl(url)) return null
        if (error != NetworkError.NotFound) return null
        return emptyPublicTreasury()
    }

    private fun emptyPublicTreasury(): T? {
        return try {
            if (responseType == PublicTreasuryModel::class.java) {
                @Suppress("UNCHECKED_CAST")
                return PublicTreasuryModel() as T
            }

            @Suppress("UNCHECKED_CAST")
            Gson().fromJson("{}", responseType) as? T
        } catch (_: Exception) {
            @Suppress("UNCHECKED_CAST")
            (PublicTreasuryModel() as? T)
        }
    }

    override fun enqueue(callback: Callback<Result<T>>) {
        val url = delegate.request().url.toString()
        val isGetRequest = delegate.request().method == "GET"
        val isRedeemCode = url.contains("/redeem-code")

        if (isGetRequest) {
            @Suppress("UNCHECKED_CAST")
            val cached = (responseType?.let { RetrofitBuilder.getCached(url, it) } ?: RetrofitBuilder.getCached<T>(url))
            if (cached != null) {
                var data: Any? = cached.data
                while (data is Result<*>) {
                    data = data.getOrNull()
                }
                if (data != null) {
                    @Suppress("UNCHECKED_CAST")
                    val resultData: T = data as T
                    callback.onResponse(
                        this@ResultCall,
                        Response.success(200, Result.success(resultData))
                    )
                    return
                }
            }
        }

        delegate.enqueue(
            object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        if (isGetRequest) {
                            response.body()?.let { body ->
                                val data = (body as? Result<*>)?.getOrNull() ?: body
                                if (RetrofitBuilder.shouldCacheSuccessBody(data)) {
                                    RetrofitBuilder.setCached(url, CachedData(data, Date()))
                                }
                            }
                        }
                        callback.onResponse(
                            this@ResultCall,
                            Response.success(
                                response.code(),
                                Result.success(successValue(response))
                            )
                        )
                    } else {
                        val errorBody = readErrorBody(response)
                        val err = NetworkError.fromHttpCode(response.code(), errorBody)
                        emptyBodyIfNotFound(url, err)?.let { empty ->
                            if (isGetRequest) {
                                RetrofitBuilder.setCached(url, CachedData(empty, Date()))
                            }
                            callback.onResponse(
                                this@ResultCall,
                                Response.success(200, Result.success(empty))
                            )
                            return
                        }
                        if (isGetRequest) {
                            @Suppress("UNCHECKED_CAST")
                            val stale: CachedData<T>? = RetrofitBuilder.getStaleGetCache<T>(url, responseType)
                            if (stale != null) {
                                MainActivity.showSnackbar(
                                    context,
                                    SnackBarType.FAILURE,
                                    err.errorMessage(stale.date, context)
                                )
                                callback.onResponse(
                                    this@ResultCall,
                                    Response.success(200, Result.success(stale.data))
                                )
                                return
                            }
                        }
                        if (!isRedeemCode) {
                            MainActivity.showSnackbar(
                                context,
                                SnackBarType.FAILURE,
                                err.errorMessage(null, context)
                            )
                        }
                        callback.onResponse(
                            this@ResultCall,
                            Response.success(
                                Result.failure(
                                    HttpException(response)
                                )
                            )
                        )
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    try {
                        if (isGetRequest) {
                            @Suppress("UNCHECKED_CAST")
                            val stale: CachedData<T>? = RetrofitBuilder.getStaleGetCache<T>(url, responseType)
                            if (stale != null) {
                                val networkErr: NetworkError = NetworkError.fromThrowable(t)
                                val errorMessage: String = networkErr.errorMessage(stale.date, context)
                                MainActivity.showSnackbar(
                                    context,
                                    SnackBarType.FAILURE,
                                    errorMessage
                                )
                                callback.onResponse(
                                    this@ResultCall,
                                    Response.success(200, Result.success(stale.data))
                                )
                                return
                            }
                        }
                        val networkErr: NetworkError = NetworkError.fromThrowable(t)
                        val errorMessage: String = networkErr.errorMessage(null, context)
                        MainActivity.showSnackbar(
                            context,
                            SnackBarType.FAILURE,
                            errorMessage
                        )
                        callback.onResponse(
                            this@ResultCall,
                            Response.success(Result.failure(RuntimeException(errorMessage, t)))
                        )
                    } catch (callbackError: Throwable) {
                        // Never let snackbar / callback mistakes escape OkHttp's thread as a fatal.
                        try {
                            callback.onResponse(
                                this@ResultCall,
                                Response.success(Result.failure(RuntimeException(t.message, t)))
                            )
                        } catch (_: Throwable) {
                            // Last resort: swallow. Parse/network failures must not kill the process.
                        }
                    }
                }
            }
        )
    }

    override fun isExecuted(): Boolean {
        return delegate.isExecuted
    }

    override fun execute(): Response<Result<T>> {
        return try {
            val response = delegate.execute()
            if (response.isSuccessful) {
                Response.success(Result.success(successValue(response)))
            } else {
                val errorBody = readErrorBody(response)
                val err = NetworkError.fromHttpCode(response.code(), errorBody)
                emptyBodyIfNotFound(delegate.request().url.toString(), err)?.let { empty ->
                    return Response.success(Result.success(empty))
                }
                Response.success(Result.failure(HttpException(response)))
            }
        } catch (t: Throwable) {
            Response.success(Result.failure(t))
        }
    }

    override fun cancel() {
        delegate.cancel()
    }

    override fun isCanceled(): Boolean {
        return delegate.isCanceled
    }

    override fun clone(): Call<Result<T>> {
        return ResultCall(delegate.clone(), context, responseType)
    }

    override fun request(): Request {
        return delegate.request()
    }

    override fun timeout(): Timeout {
        return delegate.timeout()
    }
}

private val REWRITE_RESPONSE_INTERCEPTOR = Interceptor { chain ->
    val originalResponse = chain.proceed(chain.request())
    val cacheControl = originalResponse.header("Cache-Control")

    if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") || cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")) {
        originalResponse.newBuilder()
            .removeHeader("Pragma")
            .header("Cache-Control", "public, max-age=${60 * 3}")
            .build()
    } else {
        originalResponse
    }
}

private fun REWRITE_RESPONSE_INTERCEPTOR_OFFLINE(context: Context) = Interceptor { chain ->
    var request = chain.request()

    context.getActivity()?.let {
        if (!isOnline(it)) {
            request = request.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", "public, only-if-cached")
                .build()
        }
    }

    chain.proceed(request)
}