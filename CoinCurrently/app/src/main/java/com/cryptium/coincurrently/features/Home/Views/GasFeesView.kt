package com.cryptium.coincurrently.features.Home.Views
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
import com.cryptium.coincurrently.sharedui.components.contentScreenWidth
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.theme.conditional
import com.cryptium.coincurrently.sharedui.theme.mainGreen
import com.cryptium.coincurrently.sharedui.theme.mainRed
import com.cryptium.coincurrently.sharedui.theme.mainYellow
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.core.extensions.poppinsSemibold
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.networking.ResponseModels.GasFeesModel
import com.cryptium.coincurrently.sharedui.charts.getFormattedCurrencyString
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
