package com.cryptium.coincurrently.ui.main.Compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Switches between [Row] and [Column] based on [isHorizontal], mirroring iOS `AdaptiveStack`.
 */
@Composable
fun AdaptiveStack(
    isHorizontal: Boolean,
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable AdaptiveStackScope.() -> Unit,
) {
    if (isHorizontal) {
        Row(
            modifier = modifier,
            horizontalArrangement = rowArrangement(spacing),
            verticalAlignment = verticalAlignment,
        ) {
            RowAdaptiveStackScope(isHorizontal = true, rowScope = this).content()
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = columnArrangement(spacing),
            horizontalAlignment = horizontalAlignment,
        ) {
            ColumnAdaptiveStackScope(isHorizontal = false, columnScope = this).content()
        }
    }
}

@DslMarker
annotation class AdaptiveStackScopeMarker

@AdaptiveStackScopeMarker
interface AdaptiveStackScope {
    val isHorizontal: Boolean

    /** Equal-width columns in a row, or full width in a column (mirrors iOS `.frame(maxWidth: .infinity)`). */
    fun sectionModifier(): Modifier
}

private class RowAdaptiveStackScope(
    override val isHorizontal: Boolean,
    private val rowScope: RowScope,
) : AdaptiveStackScope, RowScope by rowScope {
    override fun sectionModifier(): Modifier = Modifier.weight(1f)
}

private class ColumnAdaptiveStackScope(
    override val isHorizontal: Boolean,
    columnScope: ColumnScope,
) : AdaptiveStackScope, ColumnScope by columnScope {
    override fun sectionModifier(): Modifier = Modifier.fillMaxWidth()
}

private fun rowArrangement(spacing: Dp): Arrangement.Horizontal =
    if (spacing > 0.dp) Arrangement.spacedBy(spacing) else Arrangement.Start

private fun columnArrangement(spacing: Dp): Arrangement.Vertical =
    if (spacing > 0.dp) Arrangement.spacedBy(spacing) else Arrangement.Top
