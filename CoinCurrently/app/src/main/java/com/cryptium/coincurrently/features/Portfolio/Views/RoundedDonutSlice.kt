package com.cryptium.coincurrently.features.Portfolio.Views
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioPieChartLayout
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

internal fun roundedDonutSlicePath(
    outerStartAngle: Double,
    outerEndAngle: Double,
    innerStartAngle: Double,
    innerEndAngle: Double,
    center: Offset,
    maxRadius: Float,
    innerRadiusRatio: Float,
    innerCornerRadius: Float? = null,
    outerCornerRadius: Float? = null,
    outerRadiusScale: Float = 1f,
    explosionOffset: Float = 0f,
): Path {
    if (outerEndAngle <= outerStartAngle || innerEndAngle <= innerStartAngle) {
        return Path()
    }

    val outerRadius = maxRadius * outerRadiusScale
    val innerRadius = outerRadius * innerRadiusRatio
    val ringThickness = outerRadius - innerRadius

    val scaledInnerCorner = innerCornerRadius
        ?: maxRadius * (PortfolioPieChartLayout.innerCornerRadiusReference / PortfolioPieChartLayout.referenceChartRadius)
    val scaledOuterCorner = outerCornerRadius
        ?: maxRadius * (PortfolioPieChartLayout.outerCornerRadiusReference / PortfolioPieChartLayout.referenceChartRadius)

    val midAngle = (outerStartAngle + outerEndAngle + innerStartAngle + innerEndAngle) / 4.0
    val midAngleRadians = midAngle * PI / 180.0
    val offsetCenter = Offset(
        x = center.x + (cos(midAngleRadians) * explosionOffset).toFloat(),
        y = center.y + (sin(midAngleRadians) * explosionOffset).toFloat(),
    )

    val innerCornerR = min(scaledInnerCorner, ringThickness * 0.35f)
    val outerCornerR = min(scaledOuterCorner, ringThickness * 0.15f)

    val outerTrim = trimAngle(outerCornerR, outerRadius)
    val innerTrim = trimAngle(innerCornerR, innerRadius)

    val outerArcStart = outerStartAngle + outerTrim
    val outerArcEnd = outerEndAngle - outerTrim
    val innerArcEndSide = innerEndAngle - innerTrim
    val innerArcStartSide = innerStartAngle + innerTrim

    if (outerArcEnd <= outerArcStart || innerArcEndSide <= innerArcStartSide) {
        return annularSlicePath(
            center = offsetCenter,
            innerRadius = innerRadius,
            outerRadius = outerRadius,
            innerStartAngle = innerStartAngle,
            innerEndAngle = innerEndAngle,
            outerStartAngle = outerStartAngle,
            outerEndAngle = outerEndAngle,
        )
    }

    val path = Path()
    val outerArcStartPoint = polarPoint(offsetCenter, outerRadius, outerArcStart)
    path.moveTo(outerArcStartPoint.x, outerArcStartPoint.y)

    addArcSweep(
        path = path,
        center = offsetCenter,
        radius = outerRadius,
        startAngle = outerArcStart,
        sweepAngle = outerArcEnd - outerArcStart,
    )

    val outerEndControl = polarPoint(offsetCenter, outerRadius, outerEndAngle)
    val outerEndEdge = edgePoint(
        center = offsetCenter,
        outerRadius = outerRadius,
        innerRadius = innerRadius,
        outerDegrees = outerEndAngle,
        innerDegrees = innerEndAngle,
        inset = outerCornerR,
        fromOuterEdge = true,
    )
    path.quadraticTo(outerEndControl.x, outerEndControl.y, outerEndEdge.x, outerEndEdge.y)

    val innerEndEdge = edgePoint(
        center = offsetCenter,
        outerRadius = outerRadius,
        innerRadius = innerRadius,
        outerDegrees = outerEndAngle,
        innerDegrees = innerEndAngle,
        inset = innerCornerR,
        fromOuterEdge = false,
    )
    path.lineTo(innerEndEdge.x, innerEndEdge.y)

    val innerEndControl = polarPoint(offsetCenter, innerRadius, innerEndAngle)
    val innerArcEndPoint = polarPoint(offsetCenter, innerRadius, innerArcEndSide)
    path.quadraticTo(innerEndControl.x, innerEndControl.y, innerArcEndPoint.x, innerArcEndPoint.y)

    addArcSweep(
        path = path,
        center = offsetCenter,
        radius = innerRadius,
        startAngle = innerArcEndSide,
        sweepAngle = innerArcStartSide - innerArcEndSide,
    )

    val innerStartControl = polarPoint(offsetCenter, innerRadius, innerStartAngle)
    val innerStartEdge = edgePoint(
        center = offsetCenter,
        outerRadius = outerRadius,
        innerRadius = innerRadius,
        outerDegrees = outerStartAngle,
        innerDegrees = innerStartAngle,
        inset = innerCornerR,
        fromOuterEdge = false,
    )
    path.quadraticTo(innerStartControl.x, innerStartControl.y, innerStartEdge.x, innerStartEdge.y)

    val outerStartEdge = edgePoint(
        center = offsetCenter,
        outerRadius = outerRadius,
        innerRadius = innerRadius,
        outerDegrees = outerStartAngle,
        innerDegrees = innerStartAngle,
        inset = outerCornerR,
        fromOuterEdge = true,
    )
    path.lineTo(outerStartEdge.x, outerStartEdge.y)

    val outerStartControl = polarPoint(offsetCenter, outerRadius, outerStartAngle)
    path.quadraticTo(outerStartControl.x, outerStartControl.y, outerArcStartPoint.x, outerArcStartPoint.y)

    path.close()
    return path
}

private fun annularSlicePath(
    center: Offset,
    innerRadius: Float,
    outerRadius: Float,
    innerStartAngle: Double,
    innerEndAngle: Double,
    outerStartAngle: Double,
    outerEndAngle: Double,
): Path {
    val path = Path()
    val outerStartPoint = polarPoint(center, outerRadius, outerStartAngle)
    path.moveTo(outerStartPoint.x, outerStartPoint.y)

    addArcSweep(
        path = path,
        center = center,
        radius = outerRadius,
        startAngle = outerStartAngle,
        sweepAngle = outerEndAngle - outerStartAngle,
    )

    val innerEndPoint = polarPoint(center, innerRadius, innerEndAngle)
    path.lineTo(innerEndPoint.x, innerEndPoint.y)

    addArcSweep(
        path = path,
        center = center,
        radius = innerRadius,
        startAngle = innerEndAngle,
        sweepAngle = innerStartAngle - innerEndAngle,
    )

    path.close()
    return path
}

private fun trimAngle(cornerRadius: Float, radius: Float): Double {
    return asin(min(cornerRadius / radius, 1f).toDouble()) * 180.0 / PI
}

private fun polarPoint(center: Offset, radius: Float, degrees: Double): Offset {
    val radians = degrees * PI / 180.0
    return Offset(
        x = center.x + (radius * cos(radians)).toFloat(),
        y = center.y + (radius * sin(radians)).toFloat(),
    )
}

private fun edgePoint(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    outerDegrees: Double,
    innerDegrees: Double,
    inset: Float,
    fromOuterEdge: Boolean,
): Offset {
    val outer = polarPoint(center, outerRadius, outerDegrees)
    val inner = polarPoint(center, innerRadius, innerDegrees)
    val dx = inner.x - outer.x
    val dy = inner.y - outer.y
    val length = maxOf(sqrt((dx * dx + dy * dy).toDouble()), 0.001).toFloat()
    val ux = dx / length
    val uy = dy / length

    return if (fromOuterEdge) {
        Offset(outer.x + ux * inset, outer.y + uy * inset)
    } else {
        Offset(inner.x - ux * inset, inner.y - uy * inset)
    }
}

private fun addArcSweep(
    path: Path,
    center: Offset,
    radius: Float,
    startAngle: Double,
    sweepAngle: Double,
) {
    if (sweepAngle == 0.0) return

    val rect = Rect(
        left = center.x - radius,
        top = center.y - radius,
        right = center.x + radius,
        bottom = center.y + radius,
    )

    path.arcTo(
        rect = rect,
        startAngleDegrees = startAngle.toFloat(),
        sweepAngleDegrees = sweepAngle.toFloat(),
        forceMoveTo = false,
    )
}
