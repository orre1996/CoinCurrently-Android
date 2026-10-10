package com.cryptium.coincurrently.app

import android.content.Context
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NavigationViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Test
    fun notifyCurrencyChanged_bumpsTheRevision() {
        val viewModel = NavigationViewModel(mockk<Context>(relaxed = true))
        val before = viewModel.currencyRevisionFlow.value

        viewModel.notifyCurrencyChanged()

        assertEquals(before + 1, viewModel.currencyRevisionFlow.value)
    }

    @Test
    fun goToMarketSubTab_recordsThePageBeforeNavigation() {
        val viewModel = NavigationViewModel(mockk<Context>(relaxed = true))

        try {
            viewModel.goToMarketSubTab(3)
        } catch (_: IllegalStateException) {
            // The test host has no nav graph. The page is stored before that navigation.
        }

        assertEquals(3, viewModel.marketSelectedPage.intValue)
        assertEquals(1, viewModel.marketSubTabRequestId.intValue)
        assertEquals(2, viewModel.selectedItem.intValue)
    }
}
