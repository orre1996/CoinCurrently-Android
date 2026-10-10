package com.cryptium.coincurrently.features.PriceAlerts

import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.AlertCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.AlertDirection
import com.cryptium.coincurrently.core.networking.ResponseModels.PostAlertCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.features.Referral.ReferralRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PriceAlertsRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository, FirebaseManager, ReferralRepository)
        RetrofitBuilder.clearResponseCache()
        PriceAlertsRepository.priceAlerts = null
        PriceAlertsRepository.alertPricesAllCurrencies = null
        every { FirebaseManager.fetchFcmTokenIfNeeded() } returns true
        every { FirebaseManager.fcmToken } returns "token"
        coEvery { ReferralRepository.getUser(any()) } returns null
        coEvery { MainRepository.updateUser(any()) } returns Result.success(Unit)
        coEvery { MainRepository.subBigMover() } returns Result.success(Unit)
    }

    @Test
    fun downloadRemainingCoins_returnsNullForEmptyIds() = runTest {
        assertNull(PriceAlertsRepository.downloadRemainingCoins(emptyList()))
    }

    @Test
    fun downloadRemainingCoins_returnsNetworkCoins() = runTest {
        val coins = arrayListOf(Toplist(id = "bitcoin"))
        coEvery { MainRepository.getMultipleCurrencies("bitcoin", any()) } returns Result.success(coins)

        assertEquals(coins, PriceAlertsRepository.downloadRemainingCoins(listOf("bitcoin")))
    }

    @Test
    fun getAlertPrices_returnsNullForEmptyIds() = runTest {
        assertNull(PriceAlertsRepository.getAlertPrices(emptyList()))
    }

    @Test
    fun getAlertPrices_storesNetworkMap() = runTest {
        val prices = mutableMapOf("bitcoin" to mapOf("usd" to 1.0))
        coEvery { MainRepository.getCoinsWithAllCurrencies("bitcoin") } returns Result.success(prices)

        assertEquals(prices, PriceAlertsRepository.getAlertPrices(listOf("bitcoin")))
        assertEquals(prices, PriceAlertsRepository.alertPricesAllCurrencies)
    }

    @Test
    fun createAlert_returnsUnavailableWhenPushTokenCannotBeFetched() = runTest {
        every { FirebaseManager.fetchFcmTokenIfNeeded() } returns false

        val result = PriceAlertsRepository.createAlert(postAlert())

        assertEquals(PriceAlertCreateResult.FcmTokenUnavailable, result)
    }

    @Test
    fun createAlert_appendsCreatedAlert() = runTest {
        val created = alert("a1")
        coEvery { MainRepository.createPriceAlert(any()) } returns Result.success(created)

        val result = PriceAlertsRepository.createAlert(postAlert())

        assertEquals(PriceAlertCreateResult.Success, result)
        assertEquals(listOf(created), PriceAlertsRepository.priceAlerts)
    }

    @Test
    fun createAlert_returnsFailedWhenCreateCallFails() = runTest {
        coEvery { MainRepository.createPriceAlert(any()) } returns Result.failure(IllegalStateException("down"))

        assertEquals(PriceAlertCreateResult.Failed, PriceAlertsRepository.createAlert(postAlert()))
    }

    @Test
    fun deleteAlert_removesMatchingAlertOnSuccess() = runTest {
        PriceAlertsRepository.priceAlerts = listOf(alert("keep"), alert("drop"))
        coEvery { MainRepository.deletePriceAlert("drop") } returns Result.success(Unit)

        val remaining = PriceAlertsRepository.deleteAlert("drop")

        assertEquals(listOf("keep"), remaining?.map { it.id })
    }

    @Test
    fun deleteAlert_keepsListWhenDeleteFails() = runTest {
        val existing = listOf(alert("keep"))
        PriceAlertsRepository.priceAlerts = existing
        coEvery { MainRepository.deletePriceAlert(any()) } returns Result.failure(IllegalStateException("down"))

        assertEquals(existing, PriceAlertsRepository.deleteAlert("keep"))
    }

    @Test
    fun deleteLocalAlert_keepsRecurringAlerts() {
        val recurring = alert("recurring", resetThreshold = 1.0)
        PriceAlertsRepository.priceAlerts = listOf(recurring)

        PriceAlertsRepository.deleteLocalAlert("recurring")

        assertEquals(listOf(recurring), PriceAlertsRepository.priceAlerts)
    }

    @Test
    fun deleteLocalAlert_removesOneShotAlert() {
        PriceAlertsRepository.priceAlerts = listOf(alert("once"), alert("other"))

        PriceAlertsRepository.deleteLocalAlert("once")

        assertEquals(listOf("other"), PriceAlertsRepository.priceAlerts?.map { it.id })
    }

    @Test
    fun subBigMovers_returnsTrueOnlyWhenCallSucceeds() = runTest {
        coEvery { MainRepository.subBigMover() } returns Result.success(Unit)
        assertTrue(PriceAlertsRepository.subBigMovers())

        coEvery { MainRepository.subBigMover() } returns Result.failure(IllegalStateException("down"))
        assertFalse(PriceAlertsRepository.subBigMovers())
    }

    @Test
    fun ubsubBigMover_returnsFalseWhenUnsubscribeSucceeds() = runTest {
        coEvery { MainRepository.ubsubBigMover() } returns Result.success(Unit)

        assertFalse(PriceAlertsRepository.ubsubBigMover())
    }

    @Test
    fun sendFeedback_reportsCallSuccess() = runTest {
        coEvery { MainRepository.sendFeedback(5, "great") } returns Result.success(Unit)
        assertTrue(PriceAlertsRepository.sendFeedback(5, "great"))

        coEvery { MainRepository.sendFeedback(any(), any()) } returns Result.failure(IllegalStateException("down"))
        assertFalse(PriceAlertsRepository.sendFeedback(1, null))
    }

    @Test
    fun updateUser_skipsNetworkWhenTokenCannotBeFetched() = runTest {
        every { FirebaseManager.fetchFcmTokenIfNeeded() } returns false

        assertFalse(PriceAlertsRepository.updateUser())
    }

    private fun postAlert() = PostAlertCoin(
        coinId = "bitcoin",
        targetPrice = 100.0,
        direction = AlertDirection.Above,
        resetThreshold = null,
        preferredCurrency = "USD",
    )

    private fun alert(id: String, resetThreshold: Double? = null) = AlertCoin(
        id = id,
        resetThreshold = resetThreshold,
        direction = AlertDirection.Above,
        coinId = "bitcoin",
        targetPrice = 100.0,
        preferredCurrency = "USD",
        createdDate = "2026-01-01",
    )
}
