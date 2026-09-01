package com.cryptium.coincurrently.features.Home.Views
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.navigation.HeaderView
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.core.extensions.poppinsSemibold
import com.cryptium.coincurrently.core.networking.ResponseModels.FearGreed
import com.cryptium.coincurrently.core.networking.ResponseModels.getLastUpdated
import com.cryptium.coincurrently.core.networking.ResponseModels.getUpdatesIn
import java.util.ArrayList
import com.cryptium.coincurrently.features.FearGreed.drawIndicatorArrow
import com.cryptium.coincurrently.features.FearGreed.getResizedBitmap

@Composable
fun FearAndGreedView(
    fearGreed: FearGreed?,
    fearGreedPressed: () -> Unit
) {
    val localDensity = LocalDensity.current
    var viewHeight by remember { mutableStateOf(0.dp) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeaderView()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .primaryBackground(PaddingValues(horizontal = 16.dp)) {
                    fearGreedPressed()
                }
                .onGloballyPositioned { coordinates ->
                    viewHeight = with(localDensity) { coordinates.size.height.toDp() } - 10.dp
                }
        ) {
            Column {
                fearGreed?.data?.first()?.value?.toInt()?.let {
                    NowText(it)
                } ?: run {
                    TempNowText()
                }

                fearGreed?.getLastUpdated()?.let {
                    LastUpdatedText(it)
                } ?: run {
                    TempLastUpdatedText()
                }

                fearGreed?.getUpdatesIn()?.let {
                    NextUpdateText(it)
                } ?: run {
                    TempNextUpdateText()
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                contentAlignment = Center,
                modifier = Modifier
                    .offset(y = 4.dp)
            ) {
                PieChart(isLoading = fearGreed == null)

                PieChartArrow(
                    fearGreed?.data?.first()?.value?.toFloat(),
                    viewHeight
                )

                ArrowDotView()

                FearGreedValueText(
                    fearGreed?.data?.first()?.value ?: "0",
                    modifier = Modifier
                        .align(BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun HeaderView() {
    Text(
        text = "Fear and Greed",
        style = poppinsSemibold(24),
        color = MaterialTheme.colors.primaryTextColor,
        modifier = Modifier
            .padding(top = 24.dp, start = 16.dp)
    )
}

@Composable
private fun TempNowText() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Now: ",
            style = poppinsRegular(16),
            color = MaterialTheme.colors.primaryTextColor
        )

        Text(
            text = "Neutral",
            style = poppinsRegular(16),
            color = MaterialTheme.colors.mainGray,
            modifier = Modifier
                .shimmeringModifier(true)
        )
    }
}

@Composable
private fun NowText(
    value: Int?
) {
    Text(
        buildAnnotatedString {
            append("Now: ")
            withStyle(style = SpanStyle(getFearGreedColorCompose(value ?: 0))) {
                append(getFearGreedClarification(value ?: 0))
            }
        },
        style = poppinsRegular(16),
        color = MaterialTheme.colors.primaryTextColor,
        modifier = Modifier
            .shimmeringModifier(value == null)
            .padding(top = 16.dp)
    )
}

@Composable
private fun TempLastUpdatedText() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .padding(top = 16.dp, bottom = 2.dp)
    ) {
        Text(
            text = "Last updated ",
            style = poppinsRegular(12),
            color = MaterialTheme.colors.primaryTextColor
        )

        Text(
            text = "0h 0m ago",
            style = poppinsRegular(12),
            color = MaterialTheme.colors.mainGray,
            modifier = Modifier
                .shimmeringModifier(true)
        )
    }
}

@Composable
private fun LastUpdatedText(
    lastUpdated: String
) {
    Text("Last updated $lastUpdated",
        style = poppinsRegular(12),
        color = MaterialTheme.colors.subtitle,
        modifier = Modifier
            .padding(top = 16.dp, bottom = 2.dp)
    )
}

@Composable
private fun TempNextUpdateText() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .padding(top = 16.dp, bottom = 2.dp)
    ) {
        Text(
            text = "Next update in",
            style = poppinsRegular(12),
            color = MaterialTheme.colors.primaryTextColor
        )

        Text(
            text = "0h 0m",
            style = poppinsRegular(12),
            color = MaterialTheme.colors.mainGray,
            modifier = Modifier
                .shimmeringModifier(true)
        )
    }
}

@Composable
private fun NextUpdateText(
    nextUpdate: String?
) {
    Text(
        text = "Next update in ${nextUpdate ?: ""}",
        style = poppinsRegular(12),
        color = MaterialTheme.colors.subtitle,
        modifier = Modifier
            .shimmeringModifier(nextUpdate == null)
            .padding(bottom = 16.dp)
    )
}

@Composable
private fun PieChartArrow(
    value: Float?,
    viewHeight: Dp
) {
    val rotation = animateFloatAsState(
        targetValue = value ?: 0f,
        animationSpec = tween(
            durationMillis = 400
        ), label = ""
    )

    Image(
        bitmap = getResizedBitmap(drawIndicatorArrow(viewHeight.value.toInt(), 120.dp.value.toInt()), viewHeight.value.toInt(), 12).asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        colorFilter = ColorFilter.tint(MaterialTheme.colors.primaryTextColor),
        modifier = Modifier
            .rotate(-50.5f + (rotation.value * 2.8f))
            .height(12.dp)
            .width(viewHeight + 2.dp)
            .padding(horizontal = 8.dp)
    )
}

@Composable
private fun ArrowDotView() {
    Box(
        modifier = Modifier
            .size(size = 12.dp)
            .clip(shape = CircleShape)
            .background(MaterialTheme.colors.primaryTextColor)
    )
}

@Composable
private fun FearGreedValueText(
    value: String?,
    modifier: Modifier
) {
    Text(
        text = value ?: "0",
        style = poppinsSemibold(20),
        color = MaterialTheme.colors.primaryTextColor,
        modifier = modifier
            .fillMaxHeight()
            .offset(y = 8.dp)
            .clip(RoundedCornerShape(if (value == null) 4.dp else 0.dp))
            .background(if (value == null) MaterialTheme.colors.skeletonBackground else Color.Transparent)
            .alpha(if (value == null) 0f else 1f)
            .shimmer(1500, value == null)
    )
}

@Composable
fun PieChart(
    fromDetailPage: Boolean = false,
    isLoading: Boolean
) {
    val radiusOuter: Dp = if(fromDetailPage) 116.dp else 38.dp
    val chartBarWidth: Dp = if(fromDetailPage) 44.dp else 15.dp
    val data: MutableMap<String, Double> = HashMap()

    if (!isLoading) {
        data["0"] = 56.0
        data["1"] = 1.5
        data["2"] = 56.0
        data["3"] = 1.5
        data["4"] = 56.0
        data["5"] = 1.5
        data["6"] = 56.0
        data["7"] = 1.5
        data["8"] = 56.0
        data["9"] = 80.0
    } else {
        data["0"] = 280.0
        data["1"] = 80.0
    }

    val totalSum = data.values.sum()
    val floatValue = mutableListOf<Float>()

    data.values.forEachIndexed { index, values ->
        floatValue.add(index, 360 * values.toFloat() / totalSum.toFloat())
    }

    val colors: ArrayList<Color> = if (!isLoading) {
        arrayListOf(
            getFearGreedColorCompose(0),
            MaterialTheme.colors.transparent,
            getFearGreedColorCompose(21),
            MaterialTheme.colors.transparent,
            getFearGreedColorCompose(41),
            MaterialTheme.colors.transparent,
            getFearGreedColorCompose(61),
            MaterialTheme.colors.transparent,
            getFearGreedColorCompose(81),
        )
    } else {
        arrayListOf(
            MaterialTheme.colors.skeletonBackground
        )
    }

    colors.add(MaterialTheme.colors.transparent)

    var lastValue = 0f

    Box(
        modifier = Modifier
            .rotate(129f)
            .shimmer(1500, isLoading),
        contentAlignment = Center
    ) {
        Canvas(
            modifier = Modifier
                .size(radiusOuter * 2f)
        ) {
            floatValue.forEachIndexed { index, value ->
                drawArc(
                    color = colors[index],
                    lastValue,
                    value,
                    useCenter = false,
                    style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Butt)
                )

                lastValue += value
            }
        }
    }
}

@Composable
fun getFearGreedColorCompose(value: Int): Color {
    return if (value < 20) {
        MaterialTheme.colors.extremeFear
    } else if (value < 40) {
        MaterialTheme.colors.fear
    } else if (value < 60) {
        MaterialTheme.colors.neutral
    } else if (value < 80) {
        MaterialTheme.colors.greed
    } else {
        MaterialTheme.colors.extremeGreed
    }
}

fun getFearGreedClarification(value: Int): String {
    return if (value < 20) {
        "Extreme Fear"
    } else if (value < 40) {
        "Fear"
    } else if (value < 60) {
        "Neutral"
    } else if (value < 80) {
        "Greed"
    } else {
        "Extreme Greed"
    }
}