package com.cryptium.coincurrently.features.Settings.ViewModels
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import java.util.concurrent.Executor

enum class AuthenticationStatus {
    SUCCESS,
    FAILURE
}

class BiometricViewModel: ViewModel() {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    var isAppLocked by  mutableStateOf(false)

    fun setPrompt(
        context: Context,
        activity: FragmentActivity,
        callback: (AuthenticationStatus) -> Unit
    ) {
        executor = ContextCompat.getMainExecutor(context)
        biometricPrompt = BiometricPrompt(activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    isAppLocked = false
                    callback(AuthenticationStatus.SUCCESS)
                    super.onAuthenticationSucceeded(result)
                }

                override fun onAuthenticationFailed() {
                    callback(AuthenticationStatus.FAILURE)

                    MainActivity.showSnackbar(
                        context,
                        SnackBarType.FAILURE,
                        "Something went wrong. Make sure Face ID/Touch ID is enabled and try again."
                    )

                    super.onAuthenticationFailed()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    callback(AuthenticationStatus.FAILURE)

                    if (errString != "Cancel") {
                        MainActivity.showSnackbar(
                            context,
                            SnackBarType.FAILURE,
                            "Something went wrong. Make sure Face ID/Touch ID is enabled and try again."
                        )
                    }

                    super.onAuthenticationError(errorCode, errString)
                }
            }
        )
    }

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> { true }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> { false }
            else -> { false }
        }
    }

    fun authenticate(context: Context) {
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric authentication")
            .setSubtitle("You need to authenticate yourself to continue")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
            .build()

        if (isBiometricAvailable(context)) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            MainActivity.showSnackbar(
                context,
                SnackBarType.FAILURE,
                "Something went wrong. Make sure Face ID/Touch ID is enabled and try again."
            )
        }
    }
}