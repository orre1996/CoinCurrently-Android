package com.cryptium.coincurrently.ui.main.SettingsTab.Compose

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.ui.main.Compose.CachedUrlImage
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.*
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegular
import com.cryptium.coincurrently.ui.main.General.Components.CustomTopAppBar
import com.cryptium.coincurrently.ui.main.General.Components.EmptyContent
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.SettingsTab.ViewModels.MyWidgetsViewModel
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.Navigate
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetOpened
import com.cryptium.coincurrently.ui.main.analytics.WidgetDeleted
import com.cryptium.coincurrently.ui.main.analytics.WidgetOrderChanged
import networking.ResponseModels.Toplist
import networking.ResponseModels.stableListKey
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun MyWidgetsScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val viewModel: MyWidgetsViewModel = viewModel()
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var isHelpShowing by remember { mutableStateOf(false) }

    BackHandler(onBack = {
        navigationViewModel.navigateBack()
    })

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            CustomTopAppBar(
                title = "Widgets",
                navigateBack = {
                    navigationViewModel.navigateBack()
                },
                actions = listOf({
                    InfoButton(
                        showHelp = {
                            AnalyticsManager.log(
                                SheetOpened(ScreenLabel.Widgets, SheetLabel.WidgetsHelp),
                            )
                            isHelpShowing = true
                        }
                    )
                })
            )

            val widgets = viewModel.widgetCurrencies

            val data = remember {
                mutableStateOf(List(widgets.size) {
                    viewModel.widgetCurrencies.get(it)
                })
            }

            var didReorderThisSession by remember { mutableStateOf(false) }

            LaunchedEffect(widgets) {
                data.value = widgets.toList()
            }

            val state = rememberReorderableLazyListState(onMove = { from, to ->
                if (from.index != to.index) {
                    didReorderThisSession = true
                }
                data.value = data.value.toMutableList().apply {
                    add(to.index, removeAt(from.index))
                }
            }, onDragEnd = { _, _ ->
                if (didReorderThisSession) {
                    AnalyticsManager.log(WidgetOrderChanged(ScreenLabel.Widgets))
                    didReorderThisSession = false
                }
                viewModel.changeOrder(data.value, context)
            }, canDragOver = { draggedOver, _ ->
                data.value.any { it.stableListKey() == draggedOver.key }
            })

            LazyColumn(
                state = state.listState,
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight - 100.dp)
                    .reorderable(state)
                    .detectReorderAfterLongPress(state)
            ) {
                if (widgets.isEmpty()) {
                    item {
                        EmptyContent(
                            title = "Track prices without opening the app",
                            subTitle = "Open any coin's detail page and tap \"Add to Widget\" from the menu.",
                            buttonTitle = "Explore Coins",
                            buttonAction = {
                                navigationViewModel.navigateBack()
                                AnalyticsManager.log(
                                    Navigate(
                                        fromScreen = ScreenLabel.Widgets,
                                        toScreen = ScreenLabel.Market,
                                    ),
                                )
                                navigationViewModel.goToMarketSubTab(0)
                            },
                            modifier = Modifier
                                .padding(
                                    vertical = 40.dp,
                                    horizontal = 16.dp
                                )
                        )
                    }
                } else {
                    items(data.value, key = { it.stableListKey() }) { coin ->
                        ReorderableItem(state, key = coin.stableListKey()) { isDragging ->
                            WidgetItem(
                                coin,
                                deleteAction = {
                                    val coinName = coin?.name ?: ""
                                    AnalyticsManager.log(
                                        WidgetDeleted(
                                            fromScreen = ScreenLabel.Widgets,
                                            coinName = coinName,
                                        ),
                                    )
                                    viewModel.deleteWidget(coin, context)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (isHelpShowing) {
            WidgetHelpSheet(
                close = {
                    isHelpShowing = false
                }
            )
        }
    }
}

@Composable
private fun InfoButton(
    showHelp: () -> Unit
) {
    IconButton(onClick = {
        showHelp.invoke()
    }) {
        Image(
            painter = painterResource(id = R.drawable.info),
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                MaterialTheme.colors.onSurface
            ),
        )
    }
}

@Composable
fun WidgetItem(
    coin: Toplist?,
    deleteAction: () -> Unit,
    itemClicked: ((Toplist?) -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(vertical = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                itemClicked?.invoke(coin)
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.remove),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = Modifier
                .padding(12.dp)
                .imageSize(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    deleteAction.invoke()
                }
        )

        CachedUrlImage(
            model = coin?.image,
            size = 32.dp,
            isDownloading = coin == null,
            respectMissingContentShimmer = false,
        )

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = coin?.symbol?.uppercase() ?: "0000",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface
            )

            Text(
                text = coin?.name ?: "0000000",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDim,
            )
        }

        Image(
            painter = painterResource(id = R.drawable.list),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = Modifier
                .padding(12.dp)
                .imageSize(24.dp)
        )
    }
}

private val VerticalScrollConsumer = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource) = available.copy(x = 0f)
    override suspend fun onPreFling(available: Velocity) = available.copy(x = 0f)
}

private val HorizontalScrollConsumer = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource) = available.copy(y = 0f)
    override suspend fun onPreFling(available: Velocity) = available.copy(y = 0f)
}

fun Modifier.disabledVerticalPointerInputScroll(disabled: Boolean = true) =
    if (disabled) this.nestedScroll(VerticalScrollConsumer) else this

fun Modifier.disabledHorizontalPointerInputScroll(disabled: Boolean = true) =
    if (disabled) this.nestedScroll(HorizontalScrollConsumer) else this