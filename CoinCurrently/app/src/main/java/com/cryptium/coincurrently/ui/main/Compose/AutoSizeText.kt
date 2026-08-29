package com.cryptium.coincurrently.ui.main.Compose

import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
sealed class AutoSizeConstraint(open val min: TextUnit = TextUnit.Unspecified) {
    data class Width(override val min: TextUnit = TextUnit.Unspecified): AutoSizeConstraint(min)
    data class Height(override val min: TextUnit = TextUnit.Unspecified): AutoSizeConstraint(min)
}
@Composable
fun AutoSizeText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current,
    constraint: AutoSizeConstraint = AutoSizeConstraint.Width(),
) {
    // Reset when `text` changes; avoid LaunchedEffect + draw gating — it ran after layout and left text undrawn.
    var textStyle by remember(text) { mutableStateOf(style) }

    Text(
        modifier = modifier,
        text = text,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        style = textStyle,
        onTextLayout = { result ->
            fun constrain() {
                val currentSize = textStyle.fontSize
                val reducedSize = currentSize * 0.95f
                if (constraint.min != TextUnit.Unspecified && reducedSize <= constraint.min) {
                    textStyle = textStyle.copy(fontSize = constraint.min)
                } else if (currentSize != TextUnit.Unspecified) {
                    textStyle = textStyle.copy(fontSize = reducedSize)
                }
            }
            if (result.didOverflowHeight || result.didOverflowWidth) {
                constrain()
            }
        }
    )
}
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current,
    constraint: AutoSizeConstraint = AutoSizeConstraint.Width(),
) {
    AutoSizeText(
        modifier = modifier,
        text = AnnotatedString(text),
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        style = style,
        constraint = constraint
    )
}