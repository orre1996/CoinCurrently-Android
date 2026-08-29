package com.cryptium.coincurrently.ui.main.General

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.MinimumTextSize
import com.cryptium.coincurrently.ui.main.Compose.CappedFontScale
import com.cryptium.coincurrently.ui.main.Compose.DynamicTypeSizeCap
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceContainerDimmer
import com.cryptium.coincurrently.ui.main.Compose.outlineVariant
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.Components.Chart.hideFromTalkBackWhenEnabled

@Composable
fun ChartBackground(
    values: List<String?>,
    biggestValue: MutableState<DpSize>
) {
    val localDensity = LocalDensity.current
    val widestValue = values.maxByOrNull { it?.count() ?: 0 } ?: "000"
    var widestString by remember { mutableStateOf(0.dp) }

    Column(
        modifier = Modifier.hideFromTalkBackWhenEnabled(),
    ) {
        CappedFontScale(DynamicTypeSizeCap.XxLarge) {
        values.reversed().forEachIndexed { index, value ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.hideFromTalkBackWhenEnabled(),
            ) {
                Divider(
                    color = MaterialTheme.colors.outlineVariant,
                    modifier = Modifier
                        .weight(1f)
                        .hideFromTalkBackWhenEnabled()
                )

                MinimumTextSize(
                    text = widestValue,
                    font = satoshiBold(12),
                    alignment = Alignment.CenterStart,
                    modifier = Modifier
                        .widthIn(min = widestString)
                        .onGloballyPositioned { coordinates ->
                            val sizePx = coordinates.size
                            val width = with(localDensity) { sizePx.width.toDp() }
                            if (width > widestString) {
                                widestString = width
                                biggestValue.value = with(localDensity) { DpSize(sizePx.width.toDp() + 4.dp, sizePx.height.toDp()) }
                            }
                        }
                ) {
                    Text(
                        text = value ?: "000",
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.onSurfaceContainerDimmer,
                        modifier = Modifier
                            .shimmeringModifier(value == null)
                            .hideFromTalkBackWhenEnabled()
                    )
                }
            }

            if (index != values.count() - 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        }
    }
}