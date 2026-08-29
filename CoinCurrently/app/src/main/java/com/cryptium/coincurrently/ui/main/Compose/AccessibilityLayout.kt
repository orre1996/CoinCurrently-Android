package com.cryptium.coincurrently.ui.main.Compose

import android.content.Context
import android.content.res.Configuration
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import com.cryptium.coincurrently.ui.main.General.Components.Chart.TAB_PAGE_CONTENT_TALKBACK_TRAVERSAL_INDEX
import com.cryptium.coincurrently.ui.main.General.Components.Chart.deferTalkBackAfterChartAccessibility
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

private fun isTalkBackEnabled(context: Context): Boolean {
    val accessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
    return accessibilityManager?.isEnabled == true &&
        accessibilityManager.isTouchExplorationEnabled
}

@Composable
fun rememberTalkBackEnabled(): Boolean {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(isTalkBackEnabled(context)) }

    DisposableEffect(context) {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val touchListener = AccessibilityManager.TouchExplorationStateChangeListener {
            enabled = accessibilityManager.isEnabled && it
        }
        val stateListener = AccessibilityManager.AccessibilityStateChangeListener {
            enabled = accessibilityManager.isEnabled &&
                accessibilityManager.isTouchExplorationEnabled
        }
        accessibilityManager.addTouchExplorationStateChangeListener(touchListener)
        accessibilityManager.addAccessibilityStateChangeListener(stateListener)
        enabled = isTalkBackEnabled(context)
        onDispose {
            accessibilityManager.removeTouchExplorationStateChangeListener(touchListener)
            accessibilityManager.removeAccessibilityStateChangeListener(stateListener)
        }
    }

    return enabled
}

/**
 * Layout state derived from the user's effective font scale and screen size.
 * Mirrors iOS `!category.isAccessibilityCategory || verticalSizeClass == .compact`.
 */
data class AccessibilityLayoutState(
    val isHorizontal: Boolean,
    val useStickyHeaders: Boolean,
)

val LocalAccessibilityLayout = compositionLocalOf {
    AccessibilityLayoutState(
        isHorizontal = true,
        useStickyHeaders = true,
    )
}

private fun isAccessibilityFontScale(fontScale: Float): Boolean =
    fontScale >= DynamicTypeSizeCap.Accessibility1.fontScaleFactor

/** Maps iOS `verticalSizeClass == .compact` (limited vertical space, e.g. landscape). */
fun isCompactHeight(configuration: Configuration): Boolean =
    configuration.screenHeightDp < configuration.screenWidthDp

fun accessibilityLayoutState(
    fontScale: Float,
    configuration: Configuration,
): AccessibilityLayoutState {
    val accessibilitySize = isAccessibilityFontScale(fontScale)
    val compactHeight = isCompactHeight(configuration)
    return AccessibilityLayoutState(
        isHorizontal = !accessibilitySize || compactHeight,
        useStickyHeaders = !(compactHeight && accessibilitySize),
    )
}

/** Global accessors for composables inside [AppTheme]. */
object AccessibilityLayout {
    val isHorizontal: Boolean
        @Composable get() = LocalAccessibilityLayout.current.isHorizontal

    val useStickyHeaders: Boolean
        @Composable get() = LocalAccessibilityLayout.current.useStickyHeaders

    val isTalkBackEnabled: Boolean
        @Composable get() = rememberTalkBackEnabled()
}

/**
 * Defers tab page content until after the sticky page selector when TalkBack is enabled.
 * Use on tab content lazy items, or [tabPageItem] for single-item tab pages.
 */
@Composable
fun Modifier.tabPageTalkBackTraversal(groupChildren: Boolean = false): Modifier {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    if (!talkBackEnabled) return this

    return this.then(
        Modifier.semantics {
            traversalIndex = TAB_PAGE_CONTENT_TALKBACK_TRAVERSAL_INDEX
            if (groupChildren) {
                isTraversalGroup = true
            }
        }
    )
}

fun LazyListScope.tabPageItem(
    content: @Composable () -> Unit,
) {
    item {
        TabPageItemHost(content = content)
    }
}

@Composable
private fun TabPageItemHost(
    content: @Composable () -> Unit,
) {
    content()
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.conditionalStickyHeader(
    useSticky: Boolean,
    talkBackEnabled: Boolean,
    content: @Composable LazyItemScope.() -> Unit,
) {
    // Sticky headers pin over the list and obscure tab content from TalkBack traversal.
    if (useSticky && !talkBackEnabled) {
        stickyHeader {
            Box(modifier = Modifier.deferTalkBackAfterChartAccessibility()) {
                content()
            }
        }
    } else {
        item {
            Box(modifier = Modifier.deferTalkBackAfterChartAccessibility()) {
                content()
            }
        }
    }
}
