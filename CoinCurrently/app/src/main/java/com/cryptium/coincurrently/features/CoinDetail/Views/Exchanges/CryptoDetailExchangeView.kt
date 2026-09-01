package com.cryptium.coincurrently.features.CoinDetail.Views.Exchanges
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.mainGray
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.sharedui.theme.primaryBackground
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.networking.ResponseModels.Ticker
import com.cryptium.coincurrently.core.networking.ResponseModels.getTrustColor

@Composable
fun CryptoDetailExchangeView(
    ticker: Ticker?,
    currentPrice: Double?,
    openUrl: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .primaryBackground {
                ticker?.trade_url?.let {
                    openUrl(it)
                }
            }
    ) {
        ExchangeImageView(ticker?.trade_url)

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .weight(1f)
        ) {
            ExchangeNameView(ticker?.market?.name)

            LastPriceView(
                ticker?.converted_last?.btc,
                currentPrice
            )

            LastVolumeView(
                ticker?.converted_volume?.btc,
                currentPrice
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .width(54.dp)
        ) {
            TickerBaseView(ticker?.base)

            TickerTargetView(ticker?.target)
        }

        TrustScoreView(
            trustScoreColor = ticker?.getTrustColor
        )
    }
}

@Composable
private fun ExchangeImageView(
    imageUrl: String?
) {
    CachedUrlImage(
        model = "https://www.google.com/s2/favicons?domain=${imageUrl ?: ""}",
        size = 16.dp,
        isDownloading = imageUrl == null,
    )
}

@Composable
private fun ExchangeNameView(
    name: String?
) {
    Text(
        text = name ?: "00000",
        style = poppinsRegular(16),
        color = MaterialTheme.colors.primaryTextColor,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        modifier = Modifier
            .shimmeringModifier(name == null)
    )
}

@Composable
private fun LastPriceView(
    lastPriceBitcoin: Double?,
    currentPrice: Double?
) {
    val formattedPrice = lastPriceBitcoin?.times(currentPrice ?: 1.0)?.formatCurrency() ?: "00000000000"

    Text(
        text = "Price: $formattedPrice",
        style = poppinsRegular(14),
        color = MaterialTheme.colors.primaryTextColor,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .shimmeringModifier(currentPrice == null)
    )
}

@Composable
private fun LastVolumeView(
    lastVolumeBitcoin: Double?,
    currentPrice: Double?
) {
    val formattedPrice = lastVolumeBitcoin?.times(currentPrice ?: 1.0)?.formatCurrency() ?: "00000000000"

    Text(
        text = "Volume: $formattedPrice",
        style = poppinsRegular(14),
        color = MaterialTheme.colors.primaryTextColor,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .shimmeringModifier(currentPrice == null)
    )
}

@Composable
private fun TickerBaseView(
    base: String?
) {
    Text(
        text = base ?: "0000",
        style = poppinsRegular(16),
        color = MaterialTheme.colors.primaryTextColor,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        modifier = Modifier
            .shimmeringModifier(base == null)
    )
}

@Composable
private fun TickerTargetView(
    target: String?
) {
    Text(
        text = target ?: "0000",
        style = poppinsRegular(16),
        color = MaterialTheme.colors.primaryTextColor,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        modifier = Modifier
            .shimmeringModifier(target == null)
    )
}

@Composable
private fun TrustScoreView(
    trustScoreColor: Color?
) {
    Image(
        painter = painterResource(id = R.drawable.shield),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        colorFilter = ColorFilter.tint(trustScoreColor ?: MaterialTheme.colors.mainGray),
        modifier = Modifier
            .imageSize(26.dp)
            .shimmeringModifier(trustScoreColor == null)
    )
}