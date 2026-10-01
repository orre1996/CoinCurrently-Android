package com.cryptium.coincurrently.features.CoinDetail.Views.General
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.graphics.Bitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.sharedui.modifiers.CappedFontScale
import com.cryptium.coincurrently.sharedui.modifiers.DynamicTypeSizeCap
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.sharedui.theme.trendDown
import com.cryptium.coincurrently.sharedui.theme.trendUp
import com.cryptium.coincurrently.core.extensions.changeColor
import com.cryptium.coincurrently.core.extensions.changeWithSymbol
import com.cryptium.coincurrently.core.extensions.directionAccessible
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getChangeInValue
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.components.PriceChangeValueView
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.features.Market.Exchanges.Models.CountryFlagEmojis
import kotlinx.coroutines.delay
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import java.util.Locale

@Composable
fun CryptoDetailSummaryView(
    name: String?,
    currentPrice: Double?,
    imageUrl: String?,
    imageBitmap: Bitmap? = null,
    countryCode: String? = null,
    prices: List<Double>?,
    selectedTimeframe: TimeCharts,
    shortPrice: Boolean,
    currency: CurrencySelectionModel = UserPreferences.getSelectedCurrency(),
    valueAccessibilityLabel: String = "current price",
    changeMetricLabel: String = "price",
    changeCoinAccessibilityHint: String? = null,
    isWebSocketEnabled: Boolean = false,
    changeCoin: () -> Unit,
    modifier: Modifier
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val accessibilityFormattingThreshold = if (shortPrice || changeMetricLabel == "volume") {
        FormattingThreshold.THOUSAND
    } else {
        FormattingThreshold.NONE
    }
    val roundedChange = getDifferenceInPercent(
        prices?.lastOrNull() ?: 0.0,
        prices?.firstOrNull() ?: 0.0,
    )?.roundTo(2) ?: 0.0
    val formattedChangePercent = roundedChange.numberAccessible(maxPlaces = 2)
    val changeDirection = if (roundedChange >= 0) "up" else "down"
    val accessibilityCountryText = countryCode
        ?.takeIf { it.isNotEmpty() }
        ?.let { Locale.Builder().setRegion(it).build().displayCountry }
        ?.takeIf { it.isNotEmpty() }
        ?.let { ". Country: $it" }
        ?: ""
    val accessibilityLabel = if (changeMetricLabel == "volume") {
        val volumeText = currentPrice?.formatCurrency(
            threshold = accessibilityFormattingThreshold,
            currency = currency,
            accessible = true,
        ) ?: ""
        Localization.format(
            "%1$@, %2$@: %3$@. %4$@ %5$@ percent %6$@",
            "${name ?: "coin"}$accessibilityCountryText",
            Localization.string(valueAccessibilityLabel),
            volumeText,
            Localization.string(if (roundedChange >= 0) "Up" else "Down"),
            formattedChangePercent,
            Localization.string(selectedTimeframe.accessibilityChangePeriod),
        )
    } else {
        val currentPriceText = currentPrice?.formatCurrency(
            threshold = accessibilityFormattingThreshold,
            currency = currency,
            accessible = true,
        ) ?: ""
        val changeValueText = if (changeMetricLabel == "holdings") {
            val changeValue = roundedChange.getChangeInValue(currentValue = currentPrice ?: 0.0)
            val formattedChangeValue = kotlin.math.abs(changeValue).formatCurrency(
                threshold = accessibilityFormattingThreshold,
                currency = currency,
                accessible = true,
            )
            "$formattedChangeValue. "
        } else {
            ""
        }
        "${name ?: "coin"}$accessibilityCountryText - $valueAccessibilityLabel: $currentPriceText - ${selectedTimeframe.accessibilityTimeframe} $changeMetricLabel change. $changeDirection $changeValueText$formattedChangePercent percent"
    }
    val fullAccessibilityLabel = if (changeCoinAccessibilityHint != null) {
        "$accessibilityLabel. $changeCoinAccessibilityHint"
    } else {
        accessibilityLabel
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .then(
                if (talkBackEnabled) {
                    Modifier.clearAndSetSemantics {
                        contentDescription = fullAccessibilityLabel
                    }
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                changeCoin.invoke()
            }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CryptoImageView(
                    name = name,
                    imageUrl = imageUrl,
                    imageBitmap = imageBitmap,
                    countryCode = countryCode,
                )

                NameView(
                    name = name
                )
            }

            PriceView(
                price = currentPrice,
                shortPrice = shortPrice,
                currency = currency,
                isWebSocketEnabled = isWebSocketEnabled,
            )

            ChangeView(
                currentPrice = currentPrice,
                prices = prices,
                selectedTimeframe = selectedTimeframe,
                currency = currency
            )
        }
    }
}

@Composable
private fun CryptoImageView(
    name: String?,
    imageUrl: String?,
    imageBitmap: Bitmap?,
    countryCode: String?,
) {
    val flag = countryCode?.let { CountryFlagEmojis.flags[it] }
    if (flag != null) {
        Text(
            text = flag,
            style = satoshiBold(20),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .size(20.dp)
                .semantics { invisibleToUser() },
        )
        return
    }

    val model = imageBitmap ?: imageUrl
    CachedUrlImage(
        model = model,
        size = 24.dp,
        isDownloading = name == null,
        modifier = Modifier.semantics { invisibleToUser() },
    )
}

@Composable
private fun NameView(
    name: String?
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    Text(
        text = name ?: "000000",
        style = satoshiBold(16),
        color = MaterialTheme.colors.onSurfaceDim,
        modifier = Modifier
            .shimmeringModifier(name == null)
            .then(
                if (talkBackEnabled) Modifier.semantics { invisibleToUser() } else Modifier
            )
    )
}

@Composable
private fun PriceView(
    price: Double?,
    shortPrice: Boolean,
    currency: CurrencySelectionModel,
    isWebSocketEnabled: Boolean,
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val onSurfaceColor = MaterialTheme.colors.onSurface
    val trendUpColor = MaterialTheme.colors.trendUp
    val trendDownColor = MaterialTheme.colors.trendDown
    var priceColor by remember(isWebSocketEnabled) {
        mutableStateOf(onSurfaceColor)
    }
    var flashToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(isWebSocketEnabled, onSurfaceColor) {
        if (!isWebSocketEnabled) {
            flashToken += 1
            priceColor = onSurfaceColor
        }
    }

    val previousPrice = remember { mutableStateOf<Double?>(null) }
    LaunchedEffect(price, isWebSocketEnabled, onSurfaceColor, trendUpColor, trendDownColor) {
        if (!isWebSocketEnabled) {
            previousPrice.value = price
            return@LaunchedEffect
        }

        val oldValue = previousPrice.value
        previousPrice.value = price

        if (oldValue == null || price == null || oldValue == price) {
            return@LaunchedEffect
        }

        val oldFormatted = oldValue.formatCurrency(
            threshold = if (shortPrice) FormattingThreshold.THOUSAND else FormattingThreshold.NONE,
            currency = currency,
        )
        val newFormatted = price.formatCurrency(
            threshold = if (shortPrice) FormattingThreshold.THOUSAND else FormattingThreshold.NONE,
            currency = currency,
        )
        if (oldFormatted == newFormatted) {
            return@LaunchedEffect
        }

        priceColor = if (price > oldValue) trendUpColor else trendDownColor
        val token = flashToken + 1
        flashToken = token
        delay(350)
        if (flashToken == token) {
            priceColor = onSurfaceColor
        }
    }

    CappedFontScale(DynamicTypeSizeCap.Accessibility2) {
    Text(
        text = price?.formatCurrency(threshold = if(shortPrice) FormattingThreshold.THOUSAND else FormattingThreshold.NONE, currency = currency) ?: "000000",
        style = satoshiBold(36),
        color = if (isWebSocketEnabled) priceColor else onSurfaceColor,
        modifier = Modifier
            .shimmeringModifier(price == null)
            .then(
                if (talkBackEnabled) Modifier.semantics { invisibleToUser() } else Modifier
            )
    )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChangeView(
    currentPrice: Double?,
    prices: List<Double>?,
    selectedTimeframe: TimeCharts,
    currency: CurrencySelectionModel
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val childModifier = if (talkBackEnabled) {
        Modifier.semantics { invisibleToUser() }
    } else {
        Modifier
    }
    val roundedChange = getDifferenceInPercent(prices?.lastOrNull() ?: 0.0, prices?.firstOrNull() ?: 0.0)?.roundTo(2) ?: 0.0
    val formattedChange = "%.2f".format(roundedChange).changeWithSymbol

    val changeValue = roundedChange.getChangeInValue(currentValue = currentPrice ?: 0.0)

    FlowRow(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = childModifier,
    ) {
        PriceChangeValueView(
            change = changeValue,
            size = ChangeSize.EXTRA_SMALL,
            selectedCurrency = currency,
            isDownloading = prices.isNullOrEmpty(),
            modifier = childModifier,
        )

        Text(
            text = "($formattedChange%)",
            style = satoshiBold(12),
            color = roundedChange.changeColor(),
            modifier = Modifier
                .shimmeringModifier(prices?.isEmpty() ?: true)
                .then(childModifier)
        )

        Text(
            text = selectedTimeframe.timeAgoText,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = childModifier,
        )
    }
}