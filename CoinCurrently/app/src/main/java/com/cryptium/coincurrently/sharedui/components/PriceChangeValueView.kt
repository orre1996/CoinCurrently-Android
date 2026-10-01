package com.cryptium.coincurrently.sharedui.components
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.persistence.UserPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.changeColor
import com.cryptium.coincurrently.core.extensions.changeDecimalPoints
import com.cryptium.coincurrently.core.extensions.changeValueDecimalPoints
import com.cryptium.coincurrently.core.extensions.directionAccessible
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.changeWithSymbol
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.stars
import com.cryptium.coincurrently.core.extensions.withoutSymbol
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.sharedui.loading.PriceChangeDirectionView
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun PriceChangeValueView(
    change: Double?,
    size: ChangeSize = ChangeSize.MEDIUM,
    hideData: Boolean = false,
    showDirection: Boolean = true,
    accessibleTimeframe: String = "",
    selectedCurrency: CurrencySelectionModel = UserPreferences.getSelectedCurrency(),
    isDownloading: Boolean,
    modifier: Modifier = Modifier
) {
    val decimals = change?.changeValueDecimalPoints ?: 2
    val roundedChange = change?.roundTo(decimals)
    val direction = change?.directionAccessible ?: ""
    val localizedDirection = if (direction.isEmpty()) "" else Localization.string(direction)
    val accessibleChange = change?.numberAccessible(maxPlaces = decimals) ?: ""
    val accessibilityLabel = if (accessibleTimeframe.isEmpty()) {
        Localization.format("Price change: %@ %@ percent", localizedDirection, accessibleChange)
    } else {
        Localization.format(
            "%@ price change: %@ %@ percent",
            Localization.string(accessibleTimeframe),
            localizedDirection,
            accessibleChange,
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.clearAndSetSemantics {
            contentDescription = accessibilityLabel
        }
    ) {
        if (showDirection) {
            PriceChangeDirectionView(
                change = roundedChange,
                size = size,
                isDownloading = isDownloading
            )
        }

        Text(
            if(hideData) stars else if (showDirection) roundedChange?.formatCurrency(currency = selectedCurrency)?.withoutSymbol ?: "0000" else roundedChange?.formatCurrency()?.changeWithSymbol ?: "0000",
            style = satoshiBold(size.fontSize),
            color = roundedChange?.changeColor() ?: MaterialTheme.colors.onSurfaceDim,
            maxLines = 1,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )
    }
}