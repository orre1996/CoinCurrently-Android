package com.cryptium.coincurrently.sharedui.buttons
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.persistence.allCurrencies
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.sharedui.theme.buttonBackgroundColor
import com.cryptium.coincurrently.sharedui.theme.buttonTextColor
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainer

@Composable
fun CurrencyButton(
    title: String?,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    selectNewCurrency: () -> Unit
) {
    val currencyName = allCurrencies.firstOrNull { it.code.equals(title, ignoreCase = true) }?.currency ?: title ?: ""
    val accessibilityLabel = Localization.format(
        "Change currency, currently selected: %@",
        currencyName,
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel
                role = Role.Button
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isEnabled) {
                    selectNewCurrency.invoke()
                }
            }
    ) {
        Text(
            text = title ?: "",
            style = satoshiBold(18),
            maxLines = 1,
            color = MaterialTheme.colors.onSurfaceContainer
        )

        Image(
            painter = painterResource(R.drawable.trend_down),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceContainer),
            modifier = Modifier
                .imageSize(20.dp)
        )
    }
}