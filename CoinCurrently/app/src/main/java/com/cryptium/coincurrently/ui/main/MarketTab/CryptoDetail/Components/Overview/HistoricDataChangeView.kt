package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.Components.NewPriceChangePercentView
import com.cryptium.coincurrently.ui.main.General.Loading.ChangeSize

@Composable
fun HistoricDataChangeView(
    timeframe: String,
    change: Double?,
    modifier: Modifier
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Text(
            text = "$timeframe %",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = if (talkBackEnabled) {
                Modifier.semantics { invisibleToUser() }
            } else {
                Modifier
            },
        )

        NewPriceChangePercentView(
            change = change,
            accessibleTimeframe = timeframe.lowercase(),
            size = ChangeSize.MEDIUM,
            isDownloading = change == null,
            tabPageTalkBack = true,
        )
    }
}
