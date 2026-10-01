package com.cryptium.coincurrently.features.DerivativeDetail
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.sharedui.modifiers.MinimumTextSize
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.outlineVariant
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.core.extensions.symbolAccessible
import com.cryptium.coincurrently.core.extensions.timeUntilExpired
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.networking.ResponseModels.DerivativeDetailModel

@Composable
fun DerivativeDetailItem(
    derivativeDetail: DerivativeDetailModel?,
    multiplier: Double?,
    isFutures: Boolean,
    listIndex: Int? = null,
    listCount: Int? = null,
    imageUrl: String?,
    hasToplist: Boolean,
    derivativeSelected: () -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val isLoadingPrice = derivativeDetail?.converted_last == null || multiplier == null
    val lastPrice = (derivativeDetail?.converted_last?.usd?.toDoubleOrNull() ?: 0.0) * (multiplier ?: 0.0)
    val isLoadingOpenInterest = derivativeDetail?.open_interest_usd == null || multiplier == null
    val openInterest = (derivativeDetail?.open_interest_usd ?: 0.0) * (multiplier ?: 0.0)
    val formattedOpenInterest = if (isLoadingOpenInterest) {
        "00000000"
    } else {
        openInterest.formatCurrency()
    }
    val typeLabel = Localization.string(if (isFutures) "Futures" else "Perpetual")
    val symbol = derivativeDetail?.symbol ?: ""
    val expiredAt = derivativeDetail?.expired_at
    val expiresIn = expiredAt?.timeUntilExpired
    val isExpired = expiredAt != null &&
        java.time.Instant.ofEpochSecond(expiredAt.toLong()) <= java.time.Instant.now()
    val accessibilityLabelText = buildString {
        if (listIndex != null && listCount != null) {
            append(
                Localization.format(
                    "%1\$@ %2\$d of %3\$d, Symbol: %4\$@",
                    typeLabel,
                    listIndex + 1,
                    listCount,
                    symbol.symbolAccessible,
                )
            )
        } else {
            append("Pair: ${symbol.symbolAccessible}")
        }
        if (isFutures && expiredAt != null) {
            if (isExpired) {
                append(Localization.string(", expired"))
            } else if (expiresIn != null) {
                append(Localization.format(", expires in %@", expiresIn))
            }
        }
        append(". ")
        append(
            Localization.format(
                "Price: %@",
                lastPrice.formatCurrency(threshold = FormattingThreshold.NONE, accessible = true),
            )
        )
        append(". ")
        append(Localization.format("Open interest: %@", openInterest.formatCurrency(accessible = true)))
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabelText
                onClick(label = Localization.string("View Details"), action = {
                    derivativeSelected.invoke()
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                derivativeSelected.invoke()
            }
            .padding(
                vertical = 10.dp,
                horizontal = 16.dp
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .semantics { invisibleToUser() }
        ) {
            CoinImageView(
                imageUrl = imageUrl,
                hasToplist = hasToplist
            )

            SymbolView(
                derivative = derivativeDetail,
                isFutures = isFutures
            )
        }

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = if (isHorizontal) 24.dp else 0.dp,
            horizontalAlignment = if (isHorizontal) Alignment.Start else Alignment.End,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.semantics { invisibleToUser() },
        ) {
            Text(
                text = if (isLoadingPrice) {
                    "00000000"
                } else {
                    lastPrice.formatCurrency(threshold = FormattingThreshold.NONE)
                },
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                textAlign = if (isHorizontal) TextAlign.Start else TextAlign.End,
                modifier = Modifier
                    .shimmeringModifier(isLoadingPrice)
            )

            if (isHorizontal) {
                OpenInterestColumnView(
                    formattedOpenInterest = formattedOpenInterest,
                    isLoading = isLoadingOpenInterest,
                )
            } else {
                Text(
                    text = formattedOpenInterest,
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .shimmeringModifier(isLoadingOpenInterest)
                )
            }
        }
    }
}

@Composable
private fun CoinImageView(
    imageUrl: String?,
    hasToplist: Boolean
) {
    if (hasToplist && imageUrl == null) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.outlineVariant,
                    shape = CircleShape
                )
        )
    } else {
        CachedUrlImage(
            model = imageUrl,
            size = 28.dp,
        )
    }
}

@Composable
private fun SymbolView(
    derivative: DerivativeDetailModel?,
    isFutures: Boolean
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = derivative?.symbol ?: "000000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(derivative?.symbol == null)
        )

        if (isFutures) {
            Text(
                text = derivative?.expired_at?.timeUntilExpired ?: "-",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier
                    .shimmeringModifier(derivative == null)
            )
        }
    }
}

@Composable
private fun OpenInterestColumnView(
    formattedOpenInterest: String,
    isLoading: Boolean,
) {
    MinimumTextSize(
        text = Localization.string("Open Interest"),
        font = satoshiMedium(14),
        alignment = Alignment.CenterEnd,
        modifier = Modifier
    ) {
        Text(
            text = formattedOpenInterest,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier
                .shimmeringModifier(isLoading)
        )
    }
}
