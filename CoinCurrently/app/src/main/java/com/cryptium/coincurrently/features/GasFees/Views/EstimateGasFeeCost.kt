package com.cryptium.coincurrently.features.GasFees.Views
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization

@Composable
fun EstimateGasFeeCost(
    swapCost: String?,
    nftCost: String?,
    bridgingCost: String?,
    borrowingCost: String?,
    accessibleSwapCost: String? = null,
    accessibleNftCost: String? = null,
    accessibleBridgingCost: String? = null,
    accessibleBorrowingCost: String? = null,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .primaryBackgroundNoRipple()
    ) {
        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = 16.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top,
        ) {
            GasFeeCostView(
                title = "For Swap",
                value = swapCost,
                accessibleValue = accessibleSwapCost,
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            GasFeeCostView(
                title = "For NFT Sale",
                value = nftCost,
                accessibleValue = accessibleNftCost,
                modifier = sectionModifier(),
            )
        }

        Separator()

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = 16.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top,
        ) {
            GasFeeCostView(
                title = "For Bridging",
                value = bridgingCost,
                accessibleValue = accessibleBridgingCost,
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            GasFeeCostView(
                title = "For Borrowing",
                value = borrowingCost,
                accessibleValue = accessibleBorrowingCost,
                modifier = sectionModifier(),
            )
        }
    }
}

@Composable
private fun GasFeeCostView(
    title: String,
    value: String?,
    accessibleValue: String? = null,
    modifier: Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clearAndSetSemantics {
                contentDescription = Localization.format(
                    "Estimated cost %@",
                    Localization.string(title),
                ) + ", ${accessibleValue ?: value ?: ""}"
            }
    ) {
        Text(
            text = Localization.string(title),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Text(
            text = value ?: "000000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(value == null)
                .semantics { invisibleToUser() },
        )
    }
}
