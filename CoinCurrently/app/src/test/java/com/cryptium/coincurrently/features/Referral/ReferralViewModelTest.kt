package com.cryptium.coincurrently.features.Referral

import android.graphics.Bitmap
import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.features.Search.SearchViewModel
import com.cryptium.coincurrently.core.networking.ResponseModels.RedeemCodeResult
import com.cryptium.coincurrently.core.networking.ResponseModels.ReferralCodeResult
import com.cryptium.coincurrently.core.networking.ResponseModels.RemoteUser
import com.cryptium.coincurrently.core.networking.ResponseModels.UserMetaData
import com.cryptium.coincurrently.core.networking.ResponseModels.UserResponse
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReferralViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(ReferralRepository, FirebaseManager)
        every { ReferralRepository.user } returns null
        every { ReferralRepository.referralCodeResult } returns null
        coEvery { FirebaseManager.awaitSyncPurchases() } returns Unit
    }

    @Test
    fun loadReferralCode_ignoresASecondCallWhileLoading() = runTest {
        val bitmap = mockk<Bitmap>(relaxed = true)
        coEvery { ReferralRepository.getReferralCode() } returns ReferralCodeResult(bitmap, "CODE")
        val viewModel = ReferralViewModel()

        viewModel.loadReferralCode()

        assertEquals("CODE", viewModel.referralCode)
        assertEquals("https://coincurrently.app/invite/CODE", viewModel.referralLink)
        assertFalse(viewModel.isLoadingReferralCode)
    }

    @Test
    fun loadReferralCode_staysQuietWhenTheRepositoryHasNoCode() = runTest {
        coEvery { ReferralRepository.getReferralCode() } returns null
        val viewModel = ReferralViewModel()

        viewModel.loadReferralCode()

        assertNull(viewModel.referralCode)
        assertFalse(viewModel.isLoadingReferralCode)
    }

    @Test
    fun redeemCode_ignoresBlankInput() {
        val viewModel = ReferralViewModel(initialRedeemCodeInput = "   ")

        viewModel.redeemCode()

        assertFalse(viewModel.isRedeemingCode)
        assertFalse(viewModel.redeemSucceeded)
    }

    @Test
    fun redeemCode_clearsInputAfterSuccess() = runTest {
        coEvery { ReferralRepository.redeemCode("FRIEND") } returns RedeemCodeResult.Success
        val viewModel = ReferralViewModel(initialRedeemCodeInput = " FRIEND ")

        viewModel.redeemCode()

        assertTrue(viewModel.redeemSucceeded)
        assertEquals("", viewModel.redeemCodeInput)
        assertFalse(viewModel.showError)
    }

    @Test
    fun redeemCode_keepsTheCodeAndShowsTheError() = runTest {
        coEvery { ReferralRepository.redeemCode("NOPE") } returns RedeemCodeResult.InvalidCode
        val viewModel = ReferralViewModel(initialRedeemCodeInput = "NOPE")

        viewModel.redeemCode()

        assertEquals("NOPE", viewModel.redeemCodeInput)
        assertTrue(viewModel.showError)
        assertEquals(RedeemCodeResult.InvalidCode.errorMessage, viewModel.redeemErrorMessage)
    }

    @Test
    fun getUser_storesReferralCount() = runTest {
        coEvery { ReferralRepository.getUser(ignoreCache = true) } returns UserResponse(
            referralCount = 3,
            user = RemoteUser(
                uid = "u",
                displayName = null,
                email = null,
                phoneNumber = null,
                photoUrl = null,
                providerId = "password",
                emailVerified = false,
                disabled = false,
                providerData = emptyList(),
                tokensValidAfterTimestamp = "0",
                userMetaData = UserMetaData("0", "0", "0"),
                customClaims = null,
                tenantId = null,
            ),
        )
        val viewModel = ReferralViewModel()

        viewModel.getUser()

        assertEquals(3, viewModel.currentReferrals)
    }

    @Test
    fun factory_passesTheInitialCode() {
        val viewModel = ReferralViewModelFactory("abc").create(ReferralViewModel::class.java)

        assertEquals("abc", viewModel.redeemCodeInput)
    }

    @Test
    fun factory_rejectsUnknownTypes() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            ReferralViewModelFactory("abc").create(SearchViewModel::class.java)
        }
    }
}
