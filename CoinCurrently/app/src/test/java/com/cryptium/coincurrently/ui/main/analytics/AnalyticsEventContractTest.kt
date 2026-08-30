package com.cryptium.coincurrently.ui.main.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsEventContractTest {

    @Test
    fun searchPerformed_matchesIosEventName() {
        assertEquals("search", SearchPerformed(ScreenLabel.Toplist, "btc").eventName)
    }

    @Test
    fun tabSwitched_isMainTabsOnly() {
        val event = TabSwitched(fromTab = ScreenLabel.Home, toTab = ScreenLabel.Market)
        assertEquals("tab_switched", event.eventName)
    }

    @Test
    fun tabChanged_isInnerSegments() {
        assertEquals(
            "tab_changed",
            TabChanged(ScreenLabel.CryptoDetail, "Overview").eventName,
        )
    }
}
