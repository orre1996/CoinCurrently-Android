package com.cryptium.coincurrently.sharedui.sheets
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.buttons.CloseButton
import com.cryptium.coincurrently.sharedui.components.DragHandler
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.core.extensions.poppinsSemibold
import com.cryptium.coincurrently.sharedui.theme.primaryTextColorInverse
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHigh
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.sharedui.theme.tabbarAccent
import kotlin.math.roundToInt
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.components.rememberDragHandlerHeight
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewSheet(url: String, close: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(rememberDragHandlerHeight(topPadding = 16.dp, bottomPadding = 12.dp))
    val uriHandler = LocalUriHandler.current

    ModalBottomSheet(
        onDismissRequest = {
            close.invoke()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colors.surfaceContainerLow,
        tonalElevation = 16.dp,
        contentWindowInsets = { WindowInsets(0,0 ,0 ,0) },
        dragHandle = {
            DragHandler(
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        bottom = 12.dp
                    )
            )
        }
    ) {
        ModalBottomSheetSystemNavigationBar()
        ModalSheetBody(
            context = context,
            maxBodyHeight = maxBodyHeight,
            wrapContent = false,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {

            HeaderView(open = {
                uriHandler.openUri(url)
            }, close = {
                coroutineScope.launch {
                    sheetState.hide()
                    close.invoke()
                }
            })

            ScrollableWebView(
                url = url,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

}
/**
 * Custom WebView that exposes vertical scroll range for Compose scrollable integration.
 * Required because ModalBottomSheet's scroll container intercepts touch events - we use
 * Modifier.scrollable() to forward scroll gestures to the WebView programmatically.
 */
private class ScrollableWebViewImpl(context: Context) : WebView(context) {
    val verticalScrollRange: Int
        get() = (computeVerticalScrollRange() - height).coerceAtLeast(0)
}

@Composable
private fun ScrollableWebView(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val webView = remember(context) {
        ScrollableWebViewImpl(context).apply {
            clipToOutline = true
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.setSupportZoom(true)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }
    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }
    val scrollableState = rememberScrollableState { delta ->
        val scrollY = webView.scrollY
        val maxScroll = webView.verticalScrollRange
        if (maxScroll <= 0) return@rememberScrollableState 0f
        val newScrollY = (scrollY - delta).coerceIn(0f, maxScroll.toFloat())
        webView.scrollTo(0, newScrollY.roundToInt())
        (scrollY - webView.scrollY).toFloat()
    }
    AndroidView(
        factory = { webView },
        update = { it.loadUrl(url) },
        modifier = modifier.scrollable(scrollableState, Orientation.Vertical)
    )
}

@Composable
private fun HeaderView(
    open: () -> Unit,
    close: () -> Unit
) {
    Row(
        modifier = Modifier
                    .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.external),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(
                MaterialTheme.colors.onSurfaceDimmer
            ),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colors.surfaceContainerHigh,
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    open.invoke()
                }
                .padding(12.dp)
                .imageSize(24.dp)
        )

        Spacer(Modifier.weight(1f))

        CloseButton {
            close.invoke()
        }
    }
}
