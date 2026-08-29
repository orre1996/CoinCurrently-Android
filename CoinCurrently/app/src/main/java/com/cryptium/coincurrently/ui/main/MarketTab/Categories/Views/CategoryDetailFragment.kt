package com.cryptium.coincurrently.ui.main.MarketTab.Categories.Views

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.ui.main.Compose.*
import com.cryptium.coincurrently.ui.main.Extensions.getActivity
import com.cryptium.coincurrently.ui.main.General.Components.CustomTopAppBar
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.MarketTab.Toplist.Views.ToplistFragment
import kotlinx.coroutines.launch
import networking.ResponseModels.CryptoCategory

@Composable
fun CategoryDetailScreen(
    category: CryptoCategory,
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val toplistScrollState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers.AchievementManager.increment(
            com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementCounterKey.sectorDetailsViewed
        )
    }

    BackHandler(onBack = {
        val scrollState = toplistScrollState

        val offset = scrollState.firstVisibleItemIndex + scrollState.firstVisibleItemScrollOffset

        if (offset > 0) {
            coroutineScope.launch {
                scrollState.animateScrollToItem(0)
            }
        } else {
            navigationViewModel.navigateBack()
        }
    })

    Column(
        modifier = modifier
    ) {
        CappedFontScale(DynamicTypeSizeCap.Accessibility2) {
            CustomTopAppBar(
                title = category.name ?: "",
                navigateBack = {
                    navigationViewModel.navigateBack()
                }
            )
            ToplistFragment(
                currentPage = 0,
                category = category,
                scrollState = toplistScrollState,
                navigationViewModel = navigationViewModel
            )
        }
    }
}