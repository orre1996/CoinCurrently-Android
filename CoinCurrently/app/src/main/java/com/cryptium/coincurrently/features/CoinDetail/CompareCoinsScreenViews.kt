package com.cryptium.coincurrently.features.CoinDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.features.CoinDetail.Views.Compare.CompareRectangleView
import com.cryptium.coincurrently.sharedui.charts.LineChart
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.modifiers.imageSize
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.sharedui.theme.*

@Composable
internal fun ChangeCoinButton(
    callback: () -> Unit,
) {
    IconButton(
        onClick = { callback.invoke() },
        modifier = Modifier.semantics {
            contentDescription = "Change compare coin"
        },
    ) {
        Image(
            painter = painterResource(R.drawable.change_coin),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .imageSize(24.dp)
        )
    }
}

@Composable
internal fun CoinSymbolsView(
    mainSymbol: String?,
    compareSymbol: String?
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val hideFromTalkBack = if (talkBackEnabled) {
        Modifier.clearAndSetSemantics { invisibleToUser() }
    } else {
        Modifier
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 32.dp,
                bottom = 32.dp,
                end = 16.dp
            )
            .then(hideFromTalkBack)
    ) {
        CompareRectangleView(color = MaterialTheme.colors.primary50)

        Text(
            text = mainSymbol?.uppercase() ?: "0000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .padding(end = 24.dp)
                .shimmeringModifier(mainSymbol == null)
                .then(hideFromTalkBack)
        )

        CompareRectangleView(color = MaterialTheme.colors.blue50)

        Text(
            text = compareSymbol?.uppercase() ?: "0000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .padding(end = 24.dp)
                .shimmeringModifier(compareSymbol == null)
                .then(hideFromTalkBack)
        )
    }
}

@Composable
internal fun MainChart(
    prices: List<Double>,
    chartColor: Color?,
    dragIndex: Int?,
    chartSize: MutableState<DpSize>,
    biggestValue: DpSize,
    isDragging: Boolean = false,
    showLivePriceIndicator: Boolean = false,
) {
    val localDensity = LocalDensity.current

    LineChart(
        prices = prices,
        color = chartColor ?: MaterialTheme.colors.onSurfaceDim,
        dragIndex = dragIndex,
        showFill = false,
        dropCount = 2,
        isDragging = isDragging,
        showLivePriceIndicator = showLivePriceIndicator,
        paddings = PaddingValues(
            top = biggestValue.height / 2,
            bottom = biggestValue.height / 2,
            end = biggestValue.width
        ),
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                val sizePx = coordinates.size
                val width = floor(sizePx.width.toFloat()).toFloat()
                val height = floor(sizePx.height.toFloat()).toFloat()

                chartSize.value = with(localDensity) {
                    DpSize(width.toDp(), height.toDp())
                }
            }
    )
}

@Composable
internal fun CompareChart(
    prices: List<Double>,
    chartColor: Color?,
    dragIndex: Int?,
    biggestValue: DpSize,
    isDragging: Boolean = false,
    showLivePriceIndicator: Boolean = false,
) {
    LineChart(
        prices = prices,
        color = chartColor ?: MaterialTheme.colors.onSurfaceDim,
        dragIndex = dragIndex,
        showFill = false,
        dropCount = 2,
        isDragging = isDragging,
        showLivePriceIndicator = showLivePriceIndicator,
        paddings = PaddingValues(
            top = biggestValue.height / 2,
            bottom = biggestValue.height / 2,
            end = biggestValue.width
        ),
    )
}

