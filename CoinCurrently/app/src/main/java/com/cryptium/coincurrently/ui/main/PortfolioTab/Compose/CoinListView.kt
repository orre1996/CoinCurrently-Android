package com.cryptium.coincurrently.ui.main.PortfolioTab.Compose

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Extensions.poppinsRegular
import com.cryptium.coincurrently.ui.main.Compose.primaryBackground
import com.cryptium.coincurrently.ui.main.Compose.primaryTextColor
import networking.ResponseModels.CoinsList

@Composable
fun CoinsListItem(coin: CoinsList, callback: (CoinsList) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .primaryBackground {
                callback(coin)
            }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = coin.name ?: "",
                style = poppinsRegular(16),
                color = MaterialTheme.colors.primaryTextColor,
                textAlign = TextAlign.Start
            )

            coin.symbol?.let {
                Text(
                    text = "(${it.uppercase()})",
                    style = poppinsRegular(14),
                    color = MaterialTheme.colors.primaryTextColor,
                    textAlign = TextAlign.Start
                )
            }
        }

        Image(
            painter = painterResource(id = R.drawable.arrow_forward),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.primaryTextColor),
            modifier = Modifier
                .imageSize(20.dp, 20.dp)
        )
    }
}