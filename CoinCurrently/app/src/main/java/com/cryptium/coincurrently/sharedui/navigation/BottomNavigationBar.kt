package com.cryptium.coincurrently.sharedui.navigation
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.modifiers.CappedFontScale
import com.cryptium.coincurrently.sharedui.modifiers.DynamicTypeSizeCap
import com.cryptium.coincurrently.sharedui.theme.cardBorder
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDimmer
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmest
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.extensions.poppinsSemibold
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor
import com.cryptium.coincurrently.sharedui.theme.tabbarAccent
import com.cryptium.coincurrently.core.extensions.ignoreHorizontalParentPadding
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.app.NavigationScreens
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.TabSwitched
import com.cryptium.coincurrently.app.getRouteString

private fun screenLabelForBottomTabIndex(index: Int): ScreenLabel =
    when (index) {
        0 -> ScreenLabel.Home
        1 -> ScreenLabel.Portfolio
        2 -> ScreenLabel.Market
        3 -> ScreenLabel.News
        else -> ScreenLabel.Settings
    }

@Composable
fun BottomNavigationBar(
    navigationViewModel: NavigationViewModel,
    animationDuration: MutableState<Int>,
    navbarHeight: MutableState<Dp>,
    selectedItem: MutableState<Int>,
    showMoreTip: MutableState<Boolean>,
    isNavbarVisible: Boolean,
    systemNavigationBarBottomInset: Dp = 0.dp,
    systemNavigationBarStartInset: Dp = 0.dp,
    systemNavigationBarEndInset: Dp = 0.dp,
) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Portfolio,
        NavigationItem.Market,
        NavigationItem.News,
        NavigationItem.More
    )

    val localDensity = LocalDensity.current
    val navBackStackEntry by navigationViewModel.navHostController.currentBackStackEntryAsState()

    AnimatedVisibility(
        visible = isNavbarVisible,
        enter = slideInVertically(initialOffsetY = { w -> w }) + expandVertically(
            expandFrom = Bottom
        ),
        exit = slideOutVertically(targetOffsetY = { w -> w }) + shrinkVertically(
            shrinkTowards = Bottom
        ),
        modifier = Modifier
            .ignoreHorizontalParentPadding(1.dp)
            .offset(y = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface)
                .padding(
                    start = systemNavigationBarStartInset,
                    end = systemNavigationBarEndInset,
                    bottom = systemNavigationBarBottomInset,
                )
                .onGloballyPositioned { coordinates ->
                    val height = with(localDensity) { coordinates.size.height.toDp() }
                    navbarHeight.value = height
                }
        ) {
            Separator()

            NavigationBar(
                containerColor = MaterialTheme.colors.surface,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
            ) {
                CappedFontScale(DynamicTypeSizeCap.Large) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = index == selectedItem.value

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    if (!showMoreTip.value || index == 4) {
                                        showMoreTip.value = false

                                        val tabRouteId = item.route.getRouteString()
                                        val currentRouteId = navBackStackEntry?.destination?.route
                                            ?.split(".")
                                            ?.last()
                                        if (currentRouteId == tabRouteId) {
                                            return@clickable
                                        }

                                        animationDuration.value = 0
                                        AnalyticsManager.log(
                                            TabSwitched(
                                                fromTab = screenLabelForBottomTabIndex(selectedItem.value),
                                                toTab = screenLabelForBottomTabIndex(index),
                                            ),
                                        )
                                        selectedItem.value = index

                                        navigationViewModel.navigate(item.route) {
                                            popUpTo(NavigationScreens.Loading) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }

                                        Handler(Looper.getMainLooper()).postDelayed({
                                            animationDuration.value = 300
                                        }, 100L)
                                    }
                                }
                        ) {
                            val title = Localization.string(item.title)
                            Image(
                                painter = painterResource(item.icon),
                                contentDescription = title,
                                colorFilter = ColorFilter.tint(
                                    if (isSelected) {
                                       MaterialTheme.colors.onSurface
                                    } else {
                                        MaterialTheme.colors.onSurfaceDimmest
                                    }
                                ),
                                modifier = Modifier.imageSize(24.dp)
                            )

                            Text(
                                text = title,
                                style = satoshiMedium(14),
                                color = if (isSelected) {
                                    MaterialTheme.colors.onSurface
                                } else {
                                    MaterialTheme.colors.onSurfaceDimmest
                                }
                            )
                        }
                    }
                }
                }
            }
        }
    }
}
