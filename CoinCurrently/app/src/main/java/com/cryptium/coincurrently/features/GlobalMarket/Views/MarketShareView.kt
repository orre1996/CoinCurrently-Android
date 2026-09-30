package com.cryptium.coincurrently.features.GlobalMarket.Views
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.primary20
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.symbolAccessible
import com.cryptium.coincurrently.core.networking.ResponseModels.Percentages

@Composable
fun MarketShareView(
    percent: Percentages?,
    listIndex: Int = 0,
    listCount: Int = 0,
) {
    val roundedChange = ((percent?.percentage ?: 0.0) * 100.0).roundTo(2)
    val symbol = percent?.symbol ?: ""
    val displaySymbol = when {
        percent == null -> "000"
        symbol.uppercase() == "OTHER" -> Localization.string("Other")
        else -> symbol.uppercase()
    }
    val accessibleSymbol = if (symbol.uppercase() == "OTHER") Localization.string("Other") else symbol.symbolAccessible
    val accessibleChange = roundedChange.numberAccessible(maxPlaces = 2)
    val accessibilityLabel = Localization.format(
        "Currency %@ of %@: %@. Market share: %@ percent",
        (listIndex + 1).toString(),
        listCount.toString(),
        accessibleSymbol,
        accessibleChange,
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .widthIn(min  = 106.dp)
            .padding(
                vertical  = 4.dp,
                horizontal = 8.dp
            )
            .clearAndSetSemantics {
                contentDescription = accessibilityLabel
            }
    ) {
        val colorInt = percent?.color
        val currentColor: Color = when {
            percent == null -> MaterialTheme.colors.surfaceContainerLow
            percent.symbol?.uppercase() == "OTHER" -> MaterialTheme.colors.primary20
            colorInt != null -> Color(colorInt)
            else -> MaterialTheme.colors.surfaceContainerLow
        }

        Box(
            modifier = Modifier
                .clip(shape = CircleShape)
                .size(12.dp)
                .background(currentColor)
                .shimmeringModifier(percent == null)
        )

        Text(
            buildAnnotatedString {
                append(displaySymbol)

                withStyle(
                    style = SpanStyle(
                        fontFamily = FontFamily(Font(R.font.satoshi_bold)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurfaceDim
                    )
                ) {
                    append(" $roundedChange%")
                }
            },
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurface,
            maxLines = 1,
            modifier = Modifier
                .shimmeringModifier(percent == null)
        )
    }
}