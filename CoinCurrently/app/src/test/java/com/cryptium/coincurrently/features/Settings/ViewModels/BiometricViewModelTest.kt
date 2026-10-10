package com.cryptium.coincurrently.features.Settings.ViewModels

import android.content.Context
import androidx.biometric.BiometricManager
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BiometricViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val biometricManager = mockk<BiometricManager>()

    @Before
    fun setUp() {
        mockkStatic(BiometricManager::class)
        every { BiometricManager.from(any()) } returns biometricManager
    }

    @Test
    fun startsUnlocked() {
        assertFalse(BiometricViewModel().isAppLocked)
    }

    @Test
    fun isBiometricAvailable_isTrueOnlyForASuccessfulCheck() {
        val context = mockk<Context>(relaxed = true)
        val viewModel = BiometricViewModel()
        every { biometricManager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_SUCCESS
        assertTrue(viewModel.isBiometricAvailable(context))

        every { biometricManager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
        assertFalse(viewModel.isBiometricAvailable(context))
    }
}
