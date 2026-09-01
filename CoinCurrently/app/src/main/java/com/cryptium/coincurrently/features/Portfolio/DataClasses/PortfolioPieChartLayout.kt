package com.cryptium.coincurrently.features.Portfolio.DataClasses
object PortfolioPieChartLayout {
    const val innerRadiusRatio = 0.65f
    /** Gap width in degrees measured on the outer ring. Inner gap is scaled so arc length matches. */
    const val outerGapDegrees = 2.5
    const val minimumSlicePercentage = 0.05
    const val otherSliceId = "portfolio-pie-other"
    /** Scales the drawn chart within its container (matches iOS visual weight). */
    const val chartSizeRatio = 0.85f
    const val innerCornerRadiusReference = 8f
    const val outerCornerRadiusReference = 6f
    const val referenceChartRadius = 150f

    val innerGapDegrees: Double
        get() = outerGapDegrees / innerRadiusRatio.toDouble()
}
