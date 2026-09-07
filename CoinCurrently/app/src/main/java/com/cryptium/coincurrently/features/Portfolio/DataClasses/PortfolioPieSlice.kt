package com.cryptium.coincurrently.features.Portfolio.DataClasses
data class PortfolioPieSlice(
    val id: String,
    val symbol: String,
    val name: String,
    val percentage: Double,
    val color: String?,
    val outerStartAngle: Double,
    val outerEndAngle: Double,
    val innerStartAngle: Double,
    val innerEndAngle: Double,
)
