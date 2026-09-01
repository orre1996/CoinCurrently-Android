package com.cryptium.coincurrently.features.CoinDetail.Views.General
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.sharedui.theme.surfaceContainer
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

@Composable
fun SelectCoinView(
    toplist: Toplist?,
    isSelectable: Boolean,
    isSelected: Boolean = false,
    itemType: String = "Coin",
    index: Int? = null,
    totalCount: Int? = null,
    onClick: () -> Unit
) {
    val accessibilityLabel = buildString {
        if (index != null && totalCount != null) {
            append("$itemType ${index + 1} of $totalCount")
        }

        val name = toplist?.name ?: toplist?.symbol?.uppercase()
        if (!name.isNullOrEmpty()) {
            if (isNotEmpty()) append(", ")
            append(name)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(
                color = if (isSelected) MaterialTheme.colors.surfaceContainer else Color.Transparent,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(
                start = if(isSelectable) 4.dp else 24.dp,
                top = 18.dp,
                end = if(isSelectable) 12.dp else 24.dp,
                bottom = 18.dp,
            )
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                onClick.invoke()
            }
            .then(
                if (accessibilityLabel.isNotEmpty()) {
                    Modifier.semantics(mergeDescendants = true) {
                        contentDescription = accessibilityLabel
                        role = Role.Button
                        if (isSelected) {
                            selected = true
                        }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        if (isSelectable) {
            Image(
                painter = painterResource(R.drawable.check_circle),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),
                modifier = Modifier
                    .padding(8.dp)
                    .imageSize(24.dp)
                    .alpha(if (isSelected) 1f else 0f)
            )
        }

        if (toplist == null || toplist.image != null) {
            CachedUrlImage(
                model = toplist?.image,
                size = 28.dp,
                isDownloading = toplist == null,
                respectMissingContentShimmer = false
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(start = if(toplist == null || toplist.image != null) 8.dp else 0.dp)
        ) {
            Text(
                text = toplist?.symbol?.uppercase() ?: toplist?.name ?: "0000",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .shimmeringModifier(toplist == null)
            )

            if (toplist == null || toplist.symbol != null) {
                Text(
                    text = toplist?.name ?: "0000",
                    style = satoshiBold(if(toplist?.symbol == null) 16 else 12),
                    color = if(toplist?.symbol == null) MaterialTheme.colors.onSurfaceDimmer else MaterialTheme.colors.onSurfaceDim,
                    modifier = Modifier
                        .shimmeringModifier(toplist == null)
                )
            }
        }
    }
}