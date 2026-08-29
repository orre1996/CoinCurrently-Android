package com.cryptium.coincurrently.ui.main.HomeTab.Home.Components

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.SecondaryButton
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceContainer
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Extensions.isTablet
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegular
import com.cryptium.coincurrently.ui.main.General.Components.EmptyContent
import com.cryptium.coincurrently.ui.main.General.Enums.PriceChange
import com.cryptium.coincurrently.ui.main.HomeTab.Home.Enums.CarouselFilter
import networking.ResponseModels.Toplist

@Composable
fun ToplistCategoryView(
    toplist: List<Toplist>?,
    favoriteList: List<Toplist>?,
    showViewAll: Boolean,
    carouselFilter: CarouselFilter,
    modifier: Modifier = Modifier,
    toggleFavorite: (Toplist) -> Unit,
    navigateToCryptoDetail: (Toplist) -> Unit,
    viewAllPressed: () -> Unit,
) {
    val isTablet = LocalContext.current.isTablet()
    val buttonSize = remember { mutableStateOf(0.dp) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = if(showViewAll) 32.dp else 16.dp)
    ) {
        if (toplist != null && toplist.isEmpty()) {
            EmptyView()
        } else if (toplist != null) {
            RealList(
                toplist = toplist,
                favoriteList = favoriteList,
                navigateToCryptoDetail = {
                    navigateToCryptoDetail.invoke(it)
                },
                toggleFavorite = {
                    toggleFavorite.invoke(it)
                }
            )
        } else {
            TempList(isTablet = isTablet)
        }

        if (showViewAll) {
            ShowAllButton(
                carouselFilter = carouselFilter,
                buttonSize = buttonSize
            ) {
                viewAllPressed.invoke()
            }
        }
    }
}

@Composable
private fun EmptyView() {
    val title = "No favorites yet"
    val subTitle = "Tap the star on any coin to add it here and track it easily."
    val tip = "Tip. Long press any coin to add it to favorites."

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(44.dp),
        modifier = Modifier
            .padding(
                top = 32.dp,
                start = 16.dp,
                bottom = 32.dp,
                end = 16.dp
            )
            .clearAndSetSemantics {
                contentDescription = "$title. $subTitle. $tip"
            }
    ) {
        EmptyContent(
            title = title,
            subTitle = subTitle,
            includeAccessibility = false,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.info),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDim),
                modifier = Modifier.imageSize(20.dp)
            )

            Text(
                text = "Tip: Long press any coin to add it to favorites.",
                style = satoshiRegular(14),
                color = MaterialTheme.colors.onSurfaceDim
            )
        }
    }
}

@Composable
@SuppressLint("UnrememberedMutableState")
private fun TempList(isTablet: Boolean) {
    val count = if (isTablet) 20 else 10
    (0 until count).forEach { index ->
        ToplistCard(
            coin = null,
            isFavorited = false,
            selectedChangeOption = PriceChange.SevenDays,
            isFirstElement = index == 0,
            listIndex = index,
            listCount = count,
            navigateToCryptoDetail = { },
            toggleFavorite = { },
        )
    }
}

@Composable
private fun RealList(
    toplist: List<Toplist>,
    favoriteList: List<Toplist>?,
    navigateToCryptoDetail: (Toplist) -> Unit,
    toggleFavorite: (Toplist) -> Unit,
) {
    toplist.forEach { coin ->
        val index = toplist.indexOf(coin)
        val isFavorited = favoriteList?.any { it.id == coin.id } ?: false

        ToplistCard(
            coin = coin,
            isFavorited = isFavorited,
            selectedChangeOption = PriceChange.SevenDays,
            isFirstElement = index == 0,
            listIndex = index,
            listCount = toplist.size,
            navigateToCryptoDetail = {
                navigateToCryptoDetail.invoke(coin)
            },
            toggleFavorite = {
                toggleFavorite.invoke(coin)
            },
        )
    }
}

@Composable
private fun ShowAllButton(
    carouselFilter: CarouselFilter,
    buttonSize: MutableState<Dp>,
    viewAllPressed: () -> Unit
) {
    val localDensity = LocalDensity.current

    SecondaryButton(
        text = "View All",
        modifier = Modifier
            .padding(top = 16.dp)
            .semantics {
                contentDescription = "View all ${carouselFilter.rawValue}"
            }
            .onGloballyPositioned { coordinates ->
                val height = with(localDensity) { coordinates.size.height.toDp() }
                buttonSize.value = height
            }
    ) {
        viewAllPressed.invoke()
    }
}