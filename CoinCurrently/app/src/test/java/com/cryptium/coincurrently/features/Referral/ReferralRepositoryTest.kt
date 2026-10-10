package com.cryptium.coincurrently.features.Referral

import android.graphics.Bitmap
import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.ProviderUserData
import com.cryptium.coincurrently.core.networking.ResponseModels.RedeemCodeResult
import com.cryptium.coincurrently.core.networking.ResponseModels.ReferralCodeResult
import com.cryptium.coincurrently.core.networking.ResponseModels.RemoteUser
import com.cryptium.coincurrently.core.networking.ResponseModels.UserCustomClaims
import com.cryptium.coincurrently.core.networking.ResponseModels.UserMetaData
import com.cryptium.coincurrently.core.networking.ResponseModels.UserResponse
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReferralRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        mockkObject(FirebaseManager)
        coEvery { FirebaseManager.awaitSyncPurchases() } returns Unit
        RetrofitBuilder.clearResponseCache()
    }

    @Test
    fun getReferralCode_storesNetworkResultAndInviteLink() = runTest {
        val bitmap = mockk<Bitmap>(relaxed = true)
        val fetched = ReferralCodeResult(qrImage = bitmap, code = "OSCAR")
        coEvery { MainRepository.getReferralCode() } returns fetched

        val result = ReferralRepository.getReferralCode()

        assertEquals("OSCAR", result?.code)
        assertEquals("https://coincurrently.app/invite/OSCAR", result?.inviteLink)
        assertEquals(fetched, ReferralRepository.referralCodeResult)
    }

    @Test
    fun getReferralCode_returnsNullWhenBackendHasNoCode() = runTest {
        coEvery { MainRepository.getReferralCode() } returns null

        assertNull(ReferralRepository.getReferralCode())
    }

    @Test
    fun redeemCode_mapsSuccessAndSyncsPurchases() = runTest {
        coEvery { MainRepository.redeemCode("FRIEND") } returns Result.success(Unit)

        assertEquals(RedeemCodeResult.Success, ReferralRepository.redeemCode("FRIEND"))
        coVerify { FirebaseManager.awaitSyncPurchases() }
    }

    @Test
    fun redeemCode_mapsFailureWithoutSyncingPurchases() = runTest {
        coEvery { MainRepository.redeemCode("NOPE") } returns Result.failure(IllegalStateException("down"))

        assertEquals(RedeemCodeResult.Failure, ReferralRepository.redeemCode("NOPE"))
        coVerify(exactly = 0) { FirebaseManager.awaitSyncPurchases() }
    }

    @Test
    fun getUser_storesResponseAndSkipsCacheWhenAsked() = runTest {
        val user = sampleUser(referrals = 4)
        coEvery { MainRepository.getUser() } returns user

        val result = ReferralRepository.getUser(ignoreCache = true)

        assertEquals(user, result)
        assertEquals(user, ReferralRepository.user)
    }

    private fun sampleUser(referrals: Int) = UserResponse(
        referralCount = referrals,
        user = RemoteUser(
            uid = "uid",
            displayName = "Oscar",
            email = null,
            phoneNumber = null,
            photoUrl = null,
            providerId = "password",
            emailVerified = true,
            disabled = false,
            providerData = emptyList<ProviderUserData>(),
            tokensValidAfterTimestamp = "0",
            userMetaData = UserMetaData("0", "0", "0"),
            customClaims = UserCustomClaims(fcmToken = null, preferredCurrency = "USD"),
            tenantId = null,
        ),
    )
}
