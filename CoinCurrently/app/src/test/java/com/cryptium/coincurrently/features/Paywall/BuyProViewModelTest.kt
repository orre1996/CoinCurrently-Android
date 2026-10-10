package com.cryptium.coincurrently.features.Paywall

import com.android.billingclient.api.ProductDetails
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BuyProViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        // LocaleList is stubbed by MainDispatcherRule so button copy can resolve.
    }

    @Test
    fun subscribeButtonText_asksToUpgradeWhenATrialIsNotOffered() {
        val viewModel = BuyProViewModelImp()

        assertEquals("Upgrade Now", viewModel.subscribeButtonText)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun subscribeButtonText_offersATrialForAStandardPlan() {
        val product = mockk<ProductDetails>(relaxed = true)
        every { product.productId } returns "yearly"
        val viewModel = BuyProViewModelImp()
        viewModel.isEligibleForTrial = true
        viewModel.selectedButton = TierButton(
            title = "Yearly",
            formattedPrice = "$10",
            formattedPriceAccessibilityLabel = "10 dollars",
            product = product,
        )

        assertEquals("Start Your Free Trial", viewModel.subscribeButtonText)
    }

    @Test
    fun buyProduct_doesNothingWhileAPurchaseIsAlreadyLoading() {
        val viewModel = BuyProViewModelImp()
        viewModel.isLoading = true
        var dismissed = false

        viewModel.buyProduct(mockk(relaxed = true)) { dismissed = true }

        assertFalse(dismissed)
        assertEquals(true, viewModel.isLoading)
    }
}
