package com.cryptium.coincurrently.sharedui.components
import com.cryptium.coincurrently.core.persistence.UserPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.changeColor
import com.cryptium.coincurrently.core.extensions.changeDecimalPoints
import com.cryptium.coincurrently.core.extensions.changeValueDecimalPoints
import com.cryptium.coincurrently.core.extensions.changeWithSymbol
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.stars
import com.cryptium.coincurrently.core.extensions.withoutSymbol
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.sharedui.loading.NewShimmerImageDirection
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun NewPriceChangeValueView(
    change: Double?,
    size: ChangeSize = ChangeSize.MEDIUM,
    hideData: Boolean = false,
    showDirection: Boolean = true,
    selectedCurrency: CurrencySelectionModel = UserPreferences.getSelectedCurrency(),
    isDownloading: Boolean,
    modifier: Modifier = Modifier
) {
    val decimals = change?.changeValueDecimalPoints ?: 2
    val roundedChange = change?.roundTo(decimals)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        if (showDirection) {
            NewShimmerImageDirection(
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