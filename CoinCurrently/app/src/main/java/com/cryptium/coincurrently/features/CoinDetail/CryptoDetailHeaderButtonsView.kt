package com.cryptium.coincurrently.features.CoinDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.modifiers.imageSize
import com.cryptium.coincurrently.sharedui.theme.*

@Composable
internal fun FavoriteButton(
    isFavorited: Boolean,
    coinName: String,
    toggleFavorite: () -> Unit,
) {
    Image(
        painter = painterResource(if (isFavorited) R.drawable.favorite_filled else R.drawable.favorite_outline),
        colorFilter = ColorFilter.tint(if (isFavorited) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface),
        contentDescription = Localization.format(
            if (isFavorited) "Remove %@ from favorites" else "Add %@ to favorites",
            coinName,
        ),
        modifier = Modifier
            .padding(8.dp)
            .imageSize(24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                toggleFavorite.invoke()
            }
    )
}

@Composable
internal fun MoreButton(
    isDropDownExpanded: MutableState<Boolean>,
    coinName: String,
    compareCoins: () -> Unit,
    addPriceAlert: () -> Unit,
    shareDetailPage: () -> Unit,
) {
    IconButton(onClick = {
        isDropDownExpanded.value = true
    }) {
        Box(
            modifier = Modifier.semantics {
                contentDescription = Localization.string("More options")
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.more),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colors.onSurface
                ),
            )

            DropdownMenu(
                expanded = isDropDownExpanded.value,
                onDismissRequest = {
                    isDropDownExpanded.value = false
                }
            ) {
                DropdownMenuItem(
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.converter),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDim),
                                modifier = Modifier
                                    .imageSize(20.dp)
                            )

                            Text(
                                text = Localization.string("Compare"),
                                style = satoshiBold(16),
                                color = MaterialTheme.colors.onSurfaceDim
                            )
                        }
                    },
                    onClick = {
                        isDropDownExpanded.value = false
                        compareCoins.invoke()
                    }
                )

                Separator()

                DropdownMenuItem(
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = Localization.format("Add new %@ price alert", coinName)
                    },
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.alert),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDim),
                                modifier = Modifier
                                    .imageSize(20.dp)
                            )

                            Text(
                                text = Localization.string("Add Alert"),
                                style = satoshiBold(16),
                                color = MaterialTheme.colors.onSurfaceDim
                            )
                        }
                    },
                    onClick = {
                        isDropDownExpanded.value = false
                        addPriceAlert.invoke()
                    }
                )

                Separator()

                DropdownMenuItem(
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.share),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDim),
                                modifier = Modifier
                                    .imageSize(20.dp)
                            )

                            Text(
                                text = Localization.string("Share"),
                                style = satoshiBold(16),
                                color = MaterialTheme.colors.onSurfaceDim
                            )
                        }
                    },
                    onClick = {
                        isDropDownExpanded.value = false
                        shareDetailPage.invoke()
                    }
                )
            }
        }
    }
}

