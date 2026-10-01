package com.cryptium.coincurrently.features.CoinDetail.Views.Overview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.components.PriceChangePercentView
import com.cryptium.coincurrently.sharedui.loading.ChangeSize

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
            text = "${Localization.string(timeframe)} %",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = if (talkBackEnabled) {
                Modifier.semantics { invisibleToUser() }
            } else {
                Modifier
            },
        )

        PriceChangePercentView(
            change = change,
            accessibleTimeframe = timeframe,
            size = ChangeSize.MEDIUM,
            isDownloading = change == null,
            tabPageTalkBack = true,
        )
    }
}
