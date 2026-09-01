package com.cryptium.coincurrently.sharedui.sheets
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.rememberDragHandleHeight
import com.cryptium.coincurrently.sharedui.modifiers.DynamicTypeSizeCap

fun isEdgeToEdgeEnabled(context: Context): Int {
    val resources: Resources = context.resources
    val resourceId: Int =
        resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
    return if (resourceId > 0) {
        resources.getInteger(resourceId)
    } else {
        0
    }
}

/** Apply on the outermost layout inside [androidx.compose.material3.ModalBottomSheet] content. */
fun Modifier.modalSheetNavigationBarsPadding(context: Context): Modifier =
    if (isEdgeToEdgeEnabled(context) != 2) {
        then(Modifier.navigationBarsPadding())
    } else {
        this
    }

@Composable
fun Context.modalSheetNavigationBarExtensionHeight(density: Density): Dp {
    if (isEdgeToEdgeEnabled(this) != 2) return 0.dp

    val navBarHeightPx = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        .let { resId -> if (resId > 0) resources.getDimensionPixelSize(resId) else 0 }

    var bottomPx = maxOf(
        WindowInsets.navigationBars.getBottom(density),
        WindowInsets.systemGestures.getBottom(density),
    )
    if (bottomPx == 0) {
        bottomPx = navBarHeightPx
    }
    // Landscape 3-button nav is often on the side — only keep a real bottom inset then.
    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        val layoutDirection = LocalLayoutDirection.current
        val sideNavPx = maxOf(
            WindowInsets.navigationBars.getLeft(density, layoutDirection),
            WindowInsets.navigationBars.getRight(density, layoutDirection),
        )
        if (sideNavPx > 0) {
            bottomPx = WindowInsets.navigationBars.getBottom(density)
        }
    }
    return with(density) { bottomPx.toDp() }
}

/** Extends wrap-content sheet surfaces through the system nav area on gesture-navigation devices. */
@Composable
fun ModalSheetNavigationBarExtensionSpacer(context: Context) {
    val extensionHeight = context.modalSheetNavigationBarExtensionHeight(LocalDensity.current)
    if (extensionHeight > 0.dp) {
        Spacer(Modifier.fillMaxWidth().height(extensionHeight))
    }
}

@Composable
fun rememberModalSheetMaxBodyHeight(
    dragHandleTitle: String,
    dragHandleIcon: Int? = null,
    dragHandleBottomPadding: Dp = 24.dp,
    maxHeightFraction: Float = 0.95f,
): Dp {
    val maxSheetHeight = (LocalConfiguration.current.screenHeightDp * maxHeightFraction).dp
    val dragHandleHeight = rememberDragHandleHeight(
        title = dragHandleTitle,
        icon = dragHandleIcon,
        bottomPadding = dragHandleBottomPadding,
    )
    return (maxSheetHeight - dragHandleHeight).coerceAtLeast(0.dp)
}

@Composable
fun rememberModalSheetMaxBodyHeight(
    dragHandleHeight: Dp,
    maxHeightFraction: Float = 0.95f,
): Dp {
    val maxSheetHeight = (LocalConfiguration.current.screenHeightDp * maxHeightFraction).dp
    return (maxSheetHeight - dragHandleHeight).coerceAtLeast(0.dp)
}

/**
 * Apply to the outermost [Column] (or root layout) inside [androidx.compose.material3.ModalBottomSheet]
 * content. Do not set height on [androidx.compose.material3.ModalBottomSheet] itself.
 */
fun Modifier.modalSheetContent(
    context: Context,
    maxBodyHeight: Dp,
    wrapContent: Boolean = true,
): Modifier {
    var modifier = fillMaxWidth()
    modifier = modifier.then(
        if (wrapContent) {
            Modifier.wrapContentHeight()
        } else {
            Modifier.fillMaxHeight()
        },
    )
    return modifier.heightIn(max = maxBodyHeight)
}

@Composable
fun ModalSheetBody(
    context: Context,
    maxBodyHeight: Dp,
    wrapContent: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val extensionHeight = context.modalSheetNavigationBarExtensionHeight(LocalDensity.current)
    val contentMaxHeight = (maxBodyHeight - extensionHeight).coerceAtLeast(0.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .heightIn(max = maxBodyHeight)
            .modalSheetNavigationBarsPadding(context),
    ) {
        Column(
            modifier = Modifier.modalSheetContent(
                context = context,
                maxBodyHeight = contentMaxHeight,
                wrapContent = wrapContent,
            ),
            content = content,
        )
        ModalSheetNavigationBarExtensionSpacer(context)
    }
}

@Composable
private fun rememberModalSheetSectionHeight(
    measureKey: Any?,
    measureWidthPx: Int,
    content: @Composable () -> Unit,
    fallback: Dp,
): Dp {
    val density = LocalDensity.current
    val measuredHeightPx = remember(measureKey, measureWidthPx) { mutableIntStateOf(0) }

    Box(modifier = Modifier.size(0.dp)) {
        SubcomposeLayout { _ ->
            val placeable = subcompose("section") { content() }.first().measure(
                Constraints(
                    minWidth = measureWidthPx,
                    maxWidth = measureWidthPx,
                    minHeight = 0,
                    maxHeight = Constraints.Infinity,
                ),
            )
            measuredHeightPx.intValue = placeable.height
            layout(0, 0) {}
        }
    }

    return if (measuredHeightPx.intValue > 0) {
        with(density) { measuredHeightPx.intValue.toDp() }
    } else {
        fallback
    }
}

/**
 * [heightIn] must be on the same node as [verticalScroll] / [LazyColumn] when wrapping content.
 */
fun Modifier.modalSheetScrollRegion(maxHeight: Dp): Modifier =
    fillMaxWidth().heightIn(max = maxHeight)

/**
 * Header + scroll region + pinned footer. Wraps to content height when the list fits; switches to
 * a weighted, fixed-height scroll region when content overflows (e.g. large accessibility fonts).
 *
 * @param scrollItemCount When set, used to estimate whether the scroll region overflows without
 * pre-measuring scrollables (which cannot be measured with unbounded height).
 */
@Composable
fun ModalSheetColumnLayout(
    maxBodyHeight: Dp,
    modifier: Modifier = Modifier,
    scrollItemCount: Int? = null,
    scrollItemHeightEstimate: Dp = 80.dp,
    scrollVerticalPadding: Dp = 48.dp,
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    scrollContent: @Composable (scrollModifier: Modifier) -> Unit,
) {
    val density = LocalDensity.current
    val fontScale = density.fontScale

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .heightIn(max = maxBodyHeight),
    ) {
        // Target the declared sheet cap; parent wrap-content constraints are often smaller than
        // maxBodyHeight until the sheet content requests its full expanded height.
        val availableHeight = maxBodyHeight
        val sectionWidthPx = with(density) { maxWidth.roundToPx() }

        val headerHeight = rememberModalSheetSectionHeight(
            measureKey = fontScale to sectionWidthPx to "header",
            measureWidthPx = sectionWidthPx,
            content = header,
            fallback = 1.dp,
        )
        val footerHeight = rememberModalSheetSectionHeight(
            measureKey = fontScale to sectionWidthPx to "footer",
            measureWidthPx = sectionWidthPx,
            content = footer,
            fallback = (120.dp * fontScale).coerceAtMost(280.dp),
        )
        val scrollMaxHeight = (availableHeight - headerHeight - footerHeight).coerceAtLeast(0.dp)

        val needsFixedScrollRegion = when {
            scrollItemCount != null -> {
                val estimatedScrollHeight =
                    (scrollItemHeightEstimate * fontScale * scrollItemCount) + scrollVerticalPadding
                estimatedScrollHeight > scrollMaxHeight
            }
            fontScale >= DynamicTypeSizeCap.Accessibility1.fontScaleFactor -> true
            else -> false
        }

        if (needsFixedScrollRegion) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(availableHeight),
            ) {
                header()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    scrollContent(Modifier.fillMaxSize())
                }
                footer()
            }
        } else {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    header()
                    scrollContent(Modifier.modalSheetScrollRegion(scrollMaxHeight))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = footerHeight),
                    ) {
                        footer()
                    }
                }
            }
        }
    }
}
