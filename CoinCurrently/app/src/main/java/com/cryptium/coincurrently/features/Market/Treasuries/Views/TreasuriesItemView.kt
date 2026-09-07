package com.cryptium.coincurrently.features.Market.Treasuries.Views
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.features.Market.Exchanges.Models.CountryFlagEmojis
import com.cryptium.coincurrently.core.persistence.allCurrencies
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryHolder
import com.cryptium.coincurrently.core.networking.TreasuryEntity
import java.util.Locale

@Composable
fun TreasuriesItemView(
    data: PublicTreasuryHolder?,
    entity: TreasuryEntity? = null,
    coinSymbol: String?,
    priceMultiplier: Double?,
    listIndex: Int? = null,
    listCount: Int? = null,
    onTreasuryClick: () -> Unit,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val holdingsCurrency = holdingsCurrency(coinSymbol)
    val accessibilityHoldingsCurrency = accessibleHoldingsCurrency(coinSymbol)
    val holdingsText = data?.total_holdings?.formatCurrency(
        threshold = FormattingThreshold.NONE,
        currency = holdingsCurrency,
    ) ?: 8_437_380.0.formatCurrency(
        threshold = FormattingThreshold.MILLION,
        currency = holdingsCurrency,
    )
    val holdingsTextFiat = ((data?.total_current_value_usd ?: 0.0) * (priceMultiplier ?: 0.0)).formatCurrency(
        threshold = FormattingThreshold.NONE,
    ).let { formatted ->
        if (data == null || priceMultiplier == null) {
            55_576_689_193.0.formatCurrency(threshold = FormattingThreshold.MILLION)
        } else {
            formatted
        }
    }
    val accessibilityLabelText = buildAccessibilityLabel(
        data = data,
        entity = entity,
        coinSymbol = coinSymbol,
        priceMultiplier = priceMultiplier,
        listIndex = listIndex,
        listCount = listCount,
        holdingsCurrency = accessibilityHoldingsCurrency,
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabelText
                onClick(label = "view ${data?.name ?: ""} details", action = {
                    onTreasuryClick()
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                onTreasuryClick()
            }
            .padding(vertical = 10.dp, horizontal = 16.dp)
    ) {
        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = if (isHorizontal) 8.dp else 0.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .semantics { invisibleToUser() },
        ) {
            CountryFlagView(
                country = data?.country,
                isLoading = data?.name == null,
            )

            Text(
                text = data?.name ?: "00000",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .shimmeringModifier(data?.name == null),
            )
        }

        Column(
            horizontalAlignment = if (isHorizontal) Alignment.End else Alignment.Start,
            modifier = Modifier.semantics { invisibleToUser() },
        ) {
            Text(
                text = holdingsText,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                textAlign = if (isHorizontal) TextAlign.End else TextAlign.Start,
                modifier = Modifier.shimmeringModifier(data == null),
            )

            Text(
                text = holdingsTextFiat,
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDim,
                textAlign = if (isHorizontal) TextAlign.End else TextAlign.Start,
                modifier = Modifier.shimmeringModifier(data == null || priceMultiplier == null),
            )
        }
    }
}

@Composable
private fun CountryFlagView(
    country: String?,
    isLoading: Boolean,
) {
    val flag = country?.let { CountryFlagEmojis.flags[it] }

    Text(
        text = flag ?: country ?: "00000",
        style = satoshiBold(16),
        color = MaterialTheme.colors.onSurface,
        modifier = Modifier
            .shimmeringModifier(isLoading && flag == null)
            .semantics { invisibleToUser() },
    )
}

private fun buildAccessibilityLabel(
    data: PublicTreasuryHolder?,
    entity: TreasuryEntity?,
    coinSymbol: String?,
    priceMultiplier: Double?,
    listIndex: Int?,
    listCount: Int?,
    holdingsCurrency: CurrencySelectionModel,
): String {
    if (data == null) return "Loading treasury"

    val components = mutableListOf<String>()

    if (listIndex != null && listCount != null) {
        components.add("Treasury ${listIndex + 1} of $listCount, ${data.name ?: ""}")
    } else {
        components.add(data.name ?: "")
    }

    entity?.displayName?.let { components.add(it) }

    data?.country?.let { countryCode ->
        val countryName = Locale.Builder().setRegion(countryCode).build().displayCountry
        if (countryName.isNotEmpty()) {
            components.add("Country: $countryName")
        }
    }

    components.add(
        "Holdings: ${
            (data?.total_holdings ?: 0.0).formatCurrency(
                threshold = FormattingThreshold.NONE,
                currency = holdingsCurrency,
                accessible = true,
            )
        }"
    )

    priceMultiplier?.let { multiplier ->
        val convertedValue = (data?.total_current_value_usd ?: 0.0) * multiplier
        components.add(
            "Current value: ${convertedValue.formatCurrency(threshold = FormattingThreshold.NONE, accessible = true)}"
        )
    }

    return components.joinToString(". ")
}

private fun holdingsCurrency(coinSymbol: String?): CurrencySelectionModel {
    return CurrencySelectionModel(
        currency = "",
        code = coinSymbol?.uppercase() ?: "BTC",
        symbol = coinSymbol?.uppercase() ?: "BTC",
        suffix = true,
    )
}

private fun accessibleHoldingsCurrency(coinSymbol: String?): CurrencySelectionModel {
    val symbol = coinSymbol?.uppercase()
    if (!symbol.isNullOrEmpty()) {
        allCurrencies.firstOrNull { it.code == symbol }?.let { return it }
        return CurrencySelectionModel(
            currency = symbol.map { it.toString() }.joinToString(" "),
            code = symbol,
            symbol = symbol,
            suffix = true,
            readAccessibilityAsSymbol = true,
        )
    }

    return CurrencySelectionModel(
        currency = "",
        code = "BTC",
        symbol = "BTC",
        suffix = true,
    )
}