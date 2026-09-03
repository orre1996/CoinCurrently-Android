package com.cryptium.coincurrently.sharedui.inputs
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.charts.TAB_PAGE_CONTENT_TALKBACK_TRAVERSAL_INDEX
import com.cryptium.coincurrently.sharedui.theme.outlineVariant
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLowest
import com.cryptium.coincurrently.sharedui.modifiers.CappedFontScale
import com.cryptium.coincurrently.sharedui.modifiers.DynamicTypeSizeCap
import com.cryptium.coincurrently.core.extensions.formatCurrencyInput
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import java.text.DecimalFormat

@Composable
fun NumericInputField(
    input: Double?,
    placeholder: String = "Enter a number",
    currency: CurrencySelectionModel = UserPreferences.getSelectedCurrency(),
    isFromConverter: Boolean = false,
    forceShowCurrency: Boolean = false,
    accessibilityValue: String? = null,
    tabPageTalkBack: Boolean = false,
    maxFractionDigits: Int? = null,
    currencyInputFormat: CurrencySelectionModel? = null,
    modifier: Modifier = Modifier,
    trailingView: (@Composable () -> Unit)? = null,
    onInputChange: (Double?) -> Unit
) {
    var text by remember {
        mutableStateOf(formatNumber(input, isFromConverter, maxFractionDigits, currencyInputFormat))
    }
    var isFocused by remember { mutableStateOf(false) }
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(input, currencyInputFormat) {
        input?.let {
            val formatted = formatNumber(it, isFromConverter, maxFractionDigits, currencyInputFormat)
            if (text != formatted && !isFocused) {
                text = formatted
            }
        }
    }

    CappedFontScale(DynamicTypeSizeCap.Accessibility2) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surface)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colors.outlineVariant
                ), shape = RoundedCornerShape(8.dp)
            )
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            )
    ) {
        if ((trailingView == null && !currency.suffix) || forceShowCurrency) {
            Text(
                text = currency.symbol,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurfaceDimmer,
                modifier = Modifier.semantics { invisibleToUser() }
            )
        }

        BasicTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                parseNumericInput(newText)?.let { onInputChange(it) }
                    ?: onInputChange(null)
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    input?.let {
                        val formatted = formatNumber(it, isFromConverter, maxFractionDigits, currencyInputFormat)
                        if (text != formatted) {
                            text = formatted
                        }
                    } ?: run {
                        text = ""
                    }
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            textStyle = TextStyle(
                fontFamily = satoshiBold,
                color = MaterialTheme.colors.onSurface,
                fontSize = 16.sp,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = satoshiBold(16),
                        color = MaterialTheme.colors.onSurfaceDimmer,
                        maxLines = 1
                    )
                }

                innerTextField()
            },
            modifier = Modifier
                .weight(1f)
                .then(
                    if (accessibilityValue != null) {
                        Modifier.semantics {
                            contentDescription = accessibilityValue
                            if (tabPageTalkBack && talkBackEnabled) {
                                traversalIndex = TAB_PAGE_CONTENT_TALKBACK_TRAVERSAL_INDEX
                            }
                        }
                    } else {
                        Modifier
                    }
                )
                .onFocusChanged {
                    isFocused = it.isFocused
                    input?.let {
                        val formatted = formatNumber(it, isFromConverter, maxFractionDigits, currencyInputFormat)
                        if (text != formatted) {
                            text = formatted
                        }
                    }
                }
        )

        if (trailingView != null) {
            trailingView.invoke()
        } else if (currency.suffix) {
            Text(
                text = currency.symbol,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurfaceDimmer,
                modifier = Modifier.semantics { invisibleToUser() }
            )
        }
    }
    }
}

private fun formatNumber(
    value: Double?,
    isFromConverter: Boolean,
    maxFractionDigits: Int? = null,
    currencyInputFormat: CurrencySelectionModel? = null,
): String {
    val value = value ?: return ""

    if (currencyInputFormat != null) {
        return value.formatCurrencyInput(currency = currencyInputFormat)
    }

    val maxDecimals = maxFractionDigits ?: 10
    val minDecimals = when {
        isFromConverter -> 0
        maxFractionDigits != null -> maxFractionDigits.coerceAtMost(2)
        else -> 2
    }
    val rounded = value.roundTo(maxDecimals)

    val formatter = DecimalFormat().apply {
        minimumFractionDigits = minDecimals
        maximumFractionDigits = maxDecimals
        isGroupingUsed = !isFromConverter
    }
    return formatter.format(rounded)
}

/**
 * Parses typed/pasted amounts using structural rules only (no locale/currency).
 * Supports US/EU mixed separators, comma/dot grouping, spaces as grouping,
 * and apostrophe-style thousands (e.g. Swiss `1'234'567.89`).
 */
private fun parseNumericInput(raw: String): Double? {
    val normalized = normalizeNumericInputForParsing(raw)
    if (normalized.isEmpty() || normalized == "-") return null
    return normalized.toDoubleOrNull()
}

/** Spaces, thin spaces, and apostrophe variants used as thousands separators. */
private fun stripLocaleNeutralGroupingDecorators(s: String): String =
    s.replace("\u00A0", "")
        .replace("\u202F", "")
        .replace("\u2009", "")
        .replace("\u2007", "")
        .replace(" ", "")
        .replace("'", "")
        .replace("\u2019", "")
        .replace("\u02BC", "")

private fun normalizeNumericInputForParsing(raw: String): String {
    val trimmed = stripLocaleNeutralGroupingDecorators(raw.trim())
    if (trimmed.isEmpty()) return ""

    val filtered = buildString(trimmed.length) {
        for ((index, c) in trimmed.withIndex()) {
            when {
                c.isDigit() -> append(c)
                c == '.' || c == ',' -> append(c)
                c == '-' && index == 0 && isEmpty() -> append(c)
            }
        }
    }
    if (filtered.isEmpty() || filtered == "-") return ""

    val lastComma = filtered.lastIndexOf(',')
    val lastDot = filtered.lastIndexOf('.')

    return when {
        lastComma >= 0 && lastDot >= 0 -> {
            if (lastComma > lastDot) {
                filtered.replace(".", "").replace(',', '.')
            } else {
                filtered.replace(",", "")
            }
        }
        lastComma >= 0 -> normalizeCommaOnlySeparators(filtered)
        lastDot >= 0 -> normalizeDotOnlySeparators(filtered)
        else -> filtered
    }
}

/** Comma-only: either thousands (2,327,110) or decimal comma (12,5). */
private fun normalizeCommaOnlySeparators(s: String): String {
    val sign = if (s.startsWith("-")) "-" else ""
    val core = if (sign.isNotEmpty()) s.substring(1) else s
    val parts = core.split(',')
    if (parts.size == 1) return s
    val last = parts.last()
    val rest = parts.dropLast(1)
    val decimalComma =
        last.length <= 2 && last.all { it.isDigit() } && rest.isNotEmpty() &&
            rest.all { seg -> seg.all { ch -> ch.isDigit() } }
    val body = if (decimalComma) {
        rest.joinToString("") + "." + last
    } else {
        parts.joinToString("")
    }
    return sign + body
}

/**
 * Dot-only: multiple dots are treated as thousands grouping (e.g. `2.327.110`).
 * A single dot is always the decimal separator so values like `1.234` stay unambiguous.
 */
private fun normalizeDotOnlySeparators(s: String): String {
    val dotCount = s.count { it == '.' }
    if (dotCount <= 1) return s

    val sign = if (s.startsWith("-")) "-" else ""
    val core = if (sign.isNotEmpty()) s.substring(1) else s
    return sign + core.replace(".", "")
}