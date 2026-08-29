package com.cryptium.coincurrently.ui.main.General.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceContainerDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Extensions.changeColor
import com.cryptium.coincurrently.ui.main.Extensions.changeDecimalPoints
import com.cryptium.coincurrently.ui.main.Extensions.changeValueDecimalPoints
import com.cryptium.coincurrently.ui.main.Extensions.changeWithSymbol
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.roundTo
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.stars
import com.cryptium.coincurrently.ui.main.Extensions.withoutSymbol
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.General.Loading.ChangeSize
import com.cryptium.coincurrently.ui.main.General.Loading.NewShimmerImageDirection
import networking.ResponseModels.CurrencySelectionModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun NewPriceChangeValueView(
    change: Double?,
    size: ChangeSize = ChangeSize.MEDIUM,
    hideData: Boolean = false,
    showDirection: Boolean = true,
    selectedCurrency: CurrencySelectionModel = GlobalFunctions.getSelectedCurrency(),
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