package com.cryptium.coincurrently.ui.main.HomeTab.Compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cryptium.coincurrently.ui.main.Compose.contentScreenWidth
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.conditional
import com.cryptium.coincurrently.ui.main.Compose.mainGreen
import com.cryptium.coincurrently.ui.main.Compose.mainRed
import com.cryptium.coincurrently.ui.main.Compose.mainYellow
import com.cryptium.coincurrently.ui.main.Extensions.poppinsRegular
import com.cryptium.coincurrently.ui.main.Extensions.poppinsSemibold
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.primaryTextColor
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import networking.ResponseModels.GasFeesModel
import com.cryptium.coincurrently.ui.main.CustomViews.getFormattedCurrencyString
import java.io.Serializable

@Composable
fun GasFeesView(gasFeesModel: GasFeesModel?, fromHome: Boolean) {
    data class GasModel(val title: String, val price: String?, val color: Color): Serializable

    @Composable
    fun getGasArray(): Array<GasModel> {
        return arrayOf(
            GasModel("Slow", gasFeesModel?.gasFeeData?.firstOrNull()?.safeGasPrice, MaterialTheme.colors.mainRed),
            GasModel("Standard", gasFeesModel?.gasFeeData?.firstOrNull()?.proposeGasPrice, MaterialTheme.colors.mainYellow),
            GasModel("Fast", gasFeesModel?.gasFeeData?.firstOrNull()?.fastGasPrice, MaterialTheme.colors.mainGreen)
        )
    }

    val screenWidth = contentScreenWidth() * 0.85f

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(top = if(fromHome) 24.dp else 0.dp)
            .padding(horizontal = 16.dp)
            .conditional(!fromHome) {
                width(screenWidth)
            }
    ) {
        if (fromHome) {
            Text(
                text = "Ethereum gas fees",
                style = poppinsSemibold(24),
                color = MaterialTheme.colors.primaryTextColor
            )
        }

        Column (
            modifier = Modifier
                .primaryBackgroundNoRipple(PaddingValues(
                    vertical = if(fromHome) 16.dp else 12.dp,
                    horizontal = 16.dp)
                )
        ) {
            if (!fromHome) {
                Text(
                    text = "Ethereum gas fees",
                    style = poppinsRegular(16),
                    color = MaterialTheme.colors.primaryTextColor
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                getGasArray().forEach {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = it.title,
                            style = poppinsRegular(if(fromHome) 14 else 12),
                            color = it.color
                        )

                        Text(
                            text = "${it.price?.toDouble()?.getFormattedCurrencyString(false, minimumDigits = 2, maximumDigits = 2) ?: "000"} Gwei",
                            style = poppinsRegular(if(fromHome) 16 else 14),
                            color = MaterialTheme.colors.primaryTextColor,
                            modifier = Modifier
                                .shimmeringModifier(it.price == null)
                        )
                    }
                }
            }
        }
    }
}
