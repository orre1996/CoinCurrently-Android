package com.cryptium.coincurrently.features.CoinDetail.Views.General
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.sharedui.theme.surfaceContainer
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.extensions.alertTimeAgoDisplay
import com.cryptium.coincurrently.core.extensions.convertStringToDate
import com.cryptium.coincurrently.core.extensions.currencyAccessible
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiRegular
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.persistence.allCurrencies
import com.cryptium.coincurrently.core.networking.ResponseModels.AlertCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel

@Composable
fun CryptoDetailAlertCellView(
    symbol: String?,
    alert: AlertCoin?,
    name: String? = null,
    index: Int? = null,
    alertsCount: Int? = null,
    tabPage: Modifier = Modifier,
    deleteAlert: () -> Unit
) {
    val currency = allCurrencies.firstOrNull { it.code.lowercase() == (alert?.preferredCurrency ?: "") } ?: CurrencySelectionModel()
    val aboveOrBelow = Localization.string(
        alert?.direction?.value?.replaceFirstChar { it.uppercaseChar() } ?: "Above",
    )
    val formattedPrice = alert?.targetPrice?.formatCurrency(threshold = FormattingThreshold.NONE, currency = currency) ?: "00000000"

    val positionText = if (index != null && alertsCount != null) {
        Localization.format("Price alert %1\$d of %2\$d", index + 1, alertsCount)
    } else {
        Localization.string("Price alert")
    }

    val accessibilityLabel = if (alert == null) {
        positionText
    } else {
        val recurringText = Localization.string(if (alert.isRecurringAlert) "Recurring" else "One-time")
        val createdText = alert.createdDate.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.alertTimeAgoDisplay() ?: ""
        val directionText = Localization.string(alert.direction.value.replaceFirstChar { it.uppercaseChar() })
        val targetText = alert.targetPrice.currencyAccessible(currency = currency)
        listOf(
            positionText,
            name ?: "",
            Localization.format("Trigger when %1$@ %2$@", directionText, targetText),
            Localization.format("%@ alert", recurringText),
            Localization.format("Created %@", createdText),
        ).filter { it.isNotEmpty() }.joinToString(", ")
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = tabPage
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel
                if (alert != null) {
                    customActions = listOf(
                        CustomAccessibilityAction(Localization.string("Delete")) {
                            deleteAlert.invoke()
                            true
                        }
                    )
                }
            }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
                .semantics { invisibleToUser() }
        ) {

            Text(
                buildAnnotatedString {
                    symbol?.let {
                        withStyle(
                            style = SpanStyle(
                                fontFamily = FontFamily(Font(R.font.satoshi_bold)),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.primary
                            )
                        ) {
                            append("${it.uppercase()} ")
                        }
                    }

                    append("$aboveOrBelow ")
                    withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily(Font(R.font.satoshi_bold)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                    ) {
                        append(formattedPrice)
                    }
                },
                style = satoshiRegular(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .shimmeringModifier(alert == null)
            )

            val isRecurring = Localization.string(if (alert?.isRecurringAlert == true) "Recurring" else "One-time")
            val createdText = alert?.createdDate?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.alertTimeAgoDisplay() ?: ""

            Text(
                text = Localization.format("%1$@ · Created %2$@", isRecurring, createdText),
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDimmer,
                modifier = Modifier
                    .shimmeringModifier(alert == null)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.delete),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    deleteAlert.invoke()
                }
                .padding(16.dp)
                .imageSize(24.dp)
                .semantics { invisibleToUser() }
        )
    }
}