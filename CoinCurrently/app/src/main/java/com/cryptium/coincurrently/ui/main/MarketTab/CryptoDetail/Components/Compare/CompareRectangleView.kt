package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Compare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.General.Components.Chart.hideFromTalkBackWhenEnabled

@Composable
fun CompareRectangleView(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .hideFromTalkBackWhenEnabled()
            .size(16.dp)
            .background(
                color,
                shape = RoundedCornerShape(size = 4.dp)
            )
    )
}