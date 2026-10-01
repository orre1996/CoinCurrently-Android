package com.cryptium.coincurrently.features.Portfolio.Views
import com.cryptium.coincurrently.core.localization.Localization
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioPieChartLayout
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioPieSlice
import com.cryptium.coincurrently.sharedui.modifiers.shimmer
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.primary20
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val selectedOuterRadiusScale = 1.1f
private const val selectedExplosionOffset = 4f

private data class SliceDrawState(
    val slice: PortfolioPieSlice,
    val scale: Float,
    val explosion: Float,
    val color: Color,
)

@Composable
fun PortfolioPieChartView(
    slices: List<PortfolioPieSlice>,
    selectedSliceId: String?,
    onSelectedSliceIdChange: (String?) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (slices.isEmpty()) {
            EmptyPortfolioPieChart(modifier = Modifier.fillMaxSize())
        } else {
            InteractivePortfolioPieChart(
                slices = slices,
                selectedSliceId = selectedSliceId,
                onSelectedSliceIdChange = onSelectedSliceIdChange,
                isLoading = isLoading,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun EmptyPortfolioPieChart(modifier: Modifier = Modifier) {
    val emptyColor = MaterialTheme.colors.onSurfaceDimmer

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val chartDimension = minOf(maxWidth, maxHeight) * PortfolioPieChartLayout.chartSizeRatio

        Canvas(
            modifier = Modifier
                .size(chartDimension)
                .semantics {
                    contentDescription = Localization.string("Empty portfolio allocation chart")
                },
        ) {
            val chartSize = minOf(size.width, size.height)
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = chartSize / 2f
            val innerRadius = outerRadius * PortfolioPieChartLayout.innerRadiusRatio
            val strokeWidth = outerRadius - innerRadius

            drawCircle(
                color = emptyColor,
                radius = (outerRadius + innerRadius) / 2f,
                center = center,
                style = Stroke(width = strokeWidth),
            )
        }
    }
}

@Composable
private fun InteractivePortfolioPieChart(
    slices: List<PortfolioPieSlice>,
    selectedSliceId: String?,
    onSelectedSliceIdChange: (String?) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val selectedSlice = slices.firstOrNull { it.id == selectedSliceId }
    val loadingOverlayColor = MaterialTheme.colors.onSurface.copy(alpha = 0.25f)

    val drawStates = slices.map { slice ->
        rememberSliceDrawState(
            slice = slice,
            isSelected = slice.id == selectedSliceId,
        )
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val chartDimension = minOf(maxWidth, maxHeight) * PortfolioPieChartLayout.chartSizeRatio

        Box(
            modifier = Modifier
                .size(chartDimension)
                .pointerInput(slices, selectedSliceId) {
                    detectTapGestures { location ->
                        val chartSize = minOf(size.width.toFloat(), size.height.toFloat())
                        val center = Offset(size.width / 2f, size.height / 2f)
                        handlePieChartTap(
                            location = location,
                            chartSize = chartSize,
                            center = center,
                            slices = slices,
                            selectedSliceId = selectedSliceId,
                            onSelectedSliceIdChange = onSelectedSliceIdChange,
                        )
                    }
                },
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { invisibleToUser() }
                    .then(
                        if (isLoading) {
                            Modifier.shimmer(1500, true)
                        } else {
                            Modifier
                        },
                    ),
            ) {
            val chartSize = minOf(size.width, size.height)
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = chartSize / 2f

            drawStates.forEach { drawState ->
                val slice = drawState.slice
                val outerRadius = maxRadius * drawState.scale
                val innerRadius = outerRadius * PortfolioPieChartLayout.innerRadiusRatio
                val isFullRing = slices.size == 1 ||
                    (slice.outerEndAngle - slice.outerStartAngle) >= 359.0

                if (isFullRing) {
                    val midAngle = (slice.outerStartAngle + slice.outerEndAngle) / 2.0
                    val midAngleRadians = midAngle * PI / 180.0
                    val sliceCenter = Offset(
                        x = center.x + (cos(midAngleRadians) * drawState.explosion).toFloat(),
                        y = center.y + (sin(midAngleRadians) * drawState.explosion).toFloat(),
                    )
                    drawCircle(
                        color = drawState.color,
                        radius = (outerRadius + innerRadius) / 2f,
                        center = sliceCenter,
                        style = Stroke(width = outerRadius - innerRadius),
                    )

                    if (isLoading) {
                        drawCircle(
                            color = loadingOverlayColor,
                            radius = (outerRadius + innerRadius) / 2f,
                            center = sliceCenter,
                            style = Stroke(width = outerRadius - innerRadius),
                        )
                    }
                } else {
                    val slicePath = roundedDonutSlicePath(
                        outerStartAngle = slice.outerStartAngle,
                        outerEndAngle = slice.outerEndAngle,
                        innerStartAngle = slice.innerStartAngle,
                        innerEndAngle = slice.innerEndAngle,
                        center = center,
                        maxRadius = maxRadius,
                        innerRadiusRatio = PortfolioPieChartLayout.innerRadiusRatio,
                        outerRadiusScale = drawState.scale,
                        explosionOffset = drawState.explosion,
                    )

                    drawPath(path = slicePath, color = drawState.color)

                    if (isLoading) {
                        drawPath(path = slicePath, color = loadingOverlayColor)
                    }
                }
            }
            }

            PieSliceTalkBackOverlay(
                slices = slices,
                selectedSliceId = selectedSliceId,
                onSelectedSliceIdChange = onSelectedSliceIdChange,
            )

            selectedSlice?.let { slice ->
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clearAndSetSemantics { invisibleToUser() },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (slice.symbol == "OTHER") Localization.string("Other") else slice.symbol,
                        style = satoshiBold(18),
                        color = MaterialTheme.colors.onSurfaceDim,
                    )
                    Text(
                        text = formattedPiePercent(slice.percentage),
                        style = satoshiBold(22),
                        color = MaterialTheme.colors.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun PieSliceTalkBackOverlay(
    slices: List<PortfolioPieSlice>,
    selectedSliceId: String?,
    onSelectedSliceIdChange: (String?) -> Unit,
) {
    val minSizePx = with(LocalDensity.current) { 48.dp.toPx() }

    Layout(
        modifier = Modifier.fillMaxSize(),
        content = {
            slices.forEachIndexed { index, slice ->
                val isSelected = slice.id == selectedSliceId
                Box(
                    modifier = Modifier
                        .focusable()
                        .clearAndSetSemantics {
                            contentDescription = portfolioPieAccessibilityLabel(slice)
                            traversalIndex = index.toFloat()
                            onClick {
                                onSelectedSliceIdChange(if (isSelected) null else slice.id)
                                true
                            }
                        },
                )
            }
        },
    ) { measurables, constraints ->
        val chartSize = minOf(constraints.maxWidth, constraints.maxHeight).toFloat()
        val placeables = measurables.mapIndexed { index, measurable ->
            val bounds = sliceTalkBackBounds(
                slice = slices[index],
                chartSize = chartSize,
                minSize = minSizePx,
            )
            val width = bounds.width.roundToInt().coerceAtLeast(1)
            val height = bounds.height.roundToInt().coerceAtLeast(1)
            Triple(
                measurable.measure(Constraints.fixed(width, height)),
                bounds.left.roundToInt(),
                bounds.top.roundToInt(),
            )
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { (placeable, x, y) ->
                placeable.place(x, y)
            }
        }
    }
}

private fun sliceTalkBackBounds(
    slice: PortfolioPieSlice,
    chartSize: Float,
    minSize: Float,
): Rect {
    val center = Offset(chartSize / 2f, chartSize / 2f)
    val outerRadius = chartSize / 2f
    val innerRadius = outerRadius * PortfolioPieChartLayout.innerRadiusRatio
    val isFullRing = slice.outerEndAngle - slice.outerStartAngle >= 359.0

    if (isFullRing) {
        return Rect(0f, 0f, chartSize, chartSize)
    }

    fun pointOnCircle(angleDegrees: Double, radius: Float): Offset {
        val radians = angleDegrees * PI / 180.0
        return Offset(
            x = center.x + (cos(radians) * radius).toFloat(),
            y = center.y + (sin(radians) * radius).toFloat(),
        )
    }

    val points = buildList {
        add(pointOnCircle(slice.outerStartAngle, outerRadius))
        add(pointOnCircle(slice.outerEndAngle, outerRadius))
        add(pointOnCircle(slice.innerStartAngle, innerRadius))
        add(pointOnCircle(slice.innerEndAngle, innerRadius))

        for (cardinal in listOf(0.0, 90.0, 180.0, 270.0)) {
            if (containsAngle(cardinal, slice.outerStartAngle, slice.outerEndAngle)) {
                add(pointOnCircle(cardinal, outerRadius))
            }
            if (containsAngle(cardinal, slice.innerStartAngle, slice.innerEndAngle)) {
                add(pointOnCircle(cardinal, innerRadius))
            }
        }
    }

    var left = points.minOf { it.x }
    var top = points.minOf { it.y }
    var right = points.maxOf { it.x }
    var bottom = points.maxOf { it.y }

    if (right - left < minSize || bottom - top < minSize) {
        val midAngle = (slice.outerStartAngle + slice.outerEndAngle) / 2.0
        val midRadius = (outerRadius + innerRadius) / 2f
        val centroid = pointOnCircle(midAngle, midRadius)
        val half = minSize / 2f
        left = centroid.x - half
        top = centroid.y - half
        right = centroid.x + half
        bottom = centroid.y + half
    }

    return Rect(
        left = left.coerceIn(0f, chartSize),
        top = top.coerceIn(0f, chartSize),
        right = right.coerceIn(0f, chartSize),
        bottom = bottom.coerceIn(0f, chartSize),
    )
}

@Composable
private fun rememberSliceDrawState(
    slice: PortfolioPieSlice,
    isSelected: Boolean,
): SliceDrawState {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) selectedOuterRadiusScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "pieSliceScale",
    )
    val explosion by animateFloatAsState(
        targetValue = if (isSelected) selectedExplosionOffset else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "pieSliceExplosion",
    )

    return SliceDrawState(
        slice = slice,
        scale = scale,
        explosion = explosion,
        color = sliceDisplayColor(slice),
    )
}

@Composable
private fun sliceDisplayColor(slice: PortfolioPieSlice): Color {
    return when {
        slice.symbol == "OTHER" -> MaterialTheme.colors.primary20
        slice.color != null -> parseHexColor(slice.color) ?: MaterialTheme.colors.surfaceContainerLow
        else -> MaterialTheme.colors.surfaceContainerLow
    }
}

private fun parseHexColor(hex: String): Color? {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun formattedPiePercent(percentage: Double): String {
    val value = percentage * 100.0
    return if (value >= 99.995) {
        "100%"
    } else {
        String.format("%.2f%%", value)
    }
}

private fun portfolioPieAccessibilityLabel(slice: PortfolioPieSlice): String {
    val percentValue = slice.percentage * 100.0
    val roundedPercent = if (percentValue >= 99.995) 100.0 else percentValue.roundTo(2)
    val name = if (slice.symbol == "OTHER") Localization.string("Other") else slice.name
    return Localization.format(
        "%1\$@, %2\$@ percent of portfolio",
        name,
        roundedPercent.numberAccessible(maxPlaces = 2),
    )
}

private fun handlePieChartTap(
    location: Offset,
    chartSize: Float,
    center: Offset,
    slices: List<PortfolioPieSlice>,
    selectedSliceId: String?,
    onSelectedSliceIdChange: (String?) -> Unit,
) {
    val dx = location.x - center.x
    val dy = location.y - center.y
    val distance = sqrt(dx * dx + dy * dy)
    val outerRadius = chartSize / 2f
    val innerRadius = outerRadius * PortfolioPieChartLayout.innerRadiusRatio

    if (distance < innerRadius || distance > outerRadius * selectedOuterRadiusScale) {
        onSelectedSliceIdChange(null)
        return
    }

    if (slices.size == 1) {
        val slice = slices.first()
        onSelectedSliceIdChange(
            if (selectedSliceId == slice.id) null else slice.id,
        )
        return
    }

    val tapAngle = normalizeAngle(atan2(dy.toDouble(), dx.toDouble()) * 180.0 / PI)
    val tappedSlice = slices.firstOrNull { slice ->
        sliceContains(
            angle = tapAngle,
            distance = distance,
            slice = slice,
            outerRadius = outerRadius,
            innerRadius = innerRadius,
        )
    }

    onSelectedSliceIdChange(
        when {
            tappedSlice == null -> null
            selectedSliceId == tappedSlice.id -> null
            else -> tappedSlice.id
        },
    )
}

private fun normalizeAngle(angle: Double): Double {
    var result = angle
    while (result < 0) result += 360.0
    while (result >= 360.0) result -= 360.0
    return result
}

private fun sliceContains(
    angle: Double,
    distance: Float,
    slice: PortfolioPieSlice,
    outerRadius: Float,
    innerRadius: Float,
): Boolean {
    if (slice.outerEndAngle - slice.outerStartAngle >= 359.0) {
        return true
    }

    val ringThickness = maxOf(outerRadius - innerRadius, 1f)
    val t = ((distance - innerRadius) / ringThickness).toDouble()
    val start = lerpAngle(slice.outerStartAngle, slice.innerStartAngle, t)
    val end = lerpAngle(slice.outerEndAngle, slice.innerEndAngle, t)
    return containsAngle(angle, start, end)
}

private fun lerpAngle(outer: Double, inner: Double, t: Double): Double {
    return outer + (inner - outer) * t
}

private fun containsAngle(angle: Double, start: Double, end: Double): Boolean {
    val normalized = normalizeAngle(angle)
    val normalizedStart = normalizeAngle(start)
    val normalizedEnd = normalizeAngle(end)

    return if (normalizedStart <= normalizedEnd) {
        normalized in normalizedStart..normalizedEnd
    } else {
        normalized >= normalizedStart || normalized <= normalizedEnd
    }
}
