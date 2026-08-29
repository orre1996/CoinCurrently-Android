package com.cryptium.coincurrently.ui.main.HomeTab.Compose

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Extensions.poppinsRegular
import com.cryptium.coincurrently.ui.main.Compose.primaryTextColor
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.CustomViews.getChangeColorCompose
import com.cryptium.coincurrently.ui.main.CustomViews.getChangeSymbolResource
import com.cryptium.coincurrently.ui.main.CustomViews.getChangeWithSymbol

@Composable
fun TimeFrameChange(
    timeframe: String,
    percent: Double?,
    isDownloading: Boolean,
    fontSize: Int = 12,
    offset: Int = 4,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .offset(y = offset.dp)
    ) {
        Text(
            text = timeframe,
            color = MaterialTheme.colors.primaryTextColor,
            style = poppinsRegular(fontSize)
        )

        Text(
            text = percent?.getChangeWithSymbol() ?: "0.00%",
            color = (percent ?: 0.0).getChangeColorCompose(),
            style = poppinsRegular(fontSize),
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
        ) {
            Image(
                painter = painterResource(
                    id = (percent ?: 0.0).getChangeSymbolResource()
                ),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint((percent ?: 0.0).getChangeColorCompose()),
                modifier = Modifier
                    .shimmeringModifier(isDownloading)
                    .imageSize(fontSize.dp, (fontSize - 4).dp)
            )
        }
    }
}

@Composable
fun TimeFrameChangePortfolio(timeframe: String, change: String?, icon: Int?, color: Color, fontSize: Int = 12, font: TextStyle = poppinsRegular(12), modifier: Modifier = Modifier, hideData: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .wrapContentSize(Center)
    ) {
        if (timeframe.isNotEmpty()) {
            Text(
                text = timeframe,
                color = MaterialTheme.colors.primaryTextColor,
                style = font,
            )
        }

        Text(
            text = change ?: "0.00%",
            color = color,
            style = font,
            modifier = Modifier
                .offset(y = if(hideData) 3.dp else 0.dp)
                .shimmeringModifier(change == null)
        )

        if (icon != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(color),
                    modifier = Modifier
                        .shimmeringModifier(change == null)
                        .imageSize(fontSize.dp, (fontSize - 4).dp)
                )
            }
        }
    }
}