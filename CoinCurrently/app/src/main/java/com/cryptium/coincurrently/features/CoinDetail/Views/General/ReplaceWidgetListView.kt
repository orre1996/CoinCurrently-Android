package com.cryptium.coincurrently.features.CoinDetail.Views.General
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.outline
import com.cryptium.coincurrently.sharedui.theme.surfaceContainer
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

@Composable
fun ReplaceWidgetListView(
    coin: Toplist,
    isSelected: Boolean,
    itemClicked: (Toplist) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .background(
                color = if(isSelected) MaterialTheme.colors.surfaceContainer else Color.Transparent,
                shape = RoundedCornerShape(size = 8.dp)
            )
            .padding(
                start = 56.dp,
                top = 12.dp,
                end = 12.dp,
                bottom = 12.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                itemClicked.invoke(coin)
            }
    ) {
        CachedUrlImage(
            model = coin.image,
            size = 28.dp
        )

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = coin.symbol?.uppercase() ?: "0000",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface
            )

            Text(
                text = coin.name ?: "0000000",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDim,
            )
        }
    }
}