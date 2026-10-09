package com.cryptium.coincurrently.features.CategoryDetail
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
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.features.Market.Toplist.Views.ToplistScreen
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoCategory

@Composable
fun CategoryDetailScreen(
    category: CryptoCategory,
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val toplistScrollState = rememberLazyListState()
    val context = LocalContext.current

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
            ToplistScreen(
                currentPage = 0,
                category = category,
                scrollState = toplistScrollState,
                navigationViewModel = navigationViewModel
            )
        }
    }
}