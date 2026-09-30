package com.cryptium.coincurrently.sharedui.loading
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.extensions.getNavigationBarHeight
import com.cryptium.coincurrently.core.extensions.poppinsSemibold
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.core.extensions.pxToDp
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor
import com.cryptium.coincurrently.sharedui.theme.tabbarAccent
import com.cryptium.coincurrently.sharedui.loading.ActivityIndicator
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun LoadingScreen(
    isLoggedIn: Boolean,
    isLocked: Boolean,
    authenticate: () -> Unit
) {
    val context = LocalContext.current
    val systemNavigationBarBottomInset =
        (context.getActivity()?.getNavigationBarHeight() ?: 0).pxToDp()
    var showSpinner by remember { mutableStateOf(false) }
    val spinnerAlpha by animateFloatAsState(
        targetValue = if (showSpinner) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    LaunchedEffect(Unit) {
        delay(1.seconds)
        showSpinner = true
    }

    val bannerRes =
        if (MaterialTheme.colors.isLight) R.drawable.banner_transparent_light
        else R.drawable.banner_transparent_dark

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = bannerRes),
                contentDescription = "App banner, loading",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 64.dp, bottom = 32.dp, end = 64.dp)
            )

            Box(
                modifier = Modifier
                    .sizeIn(minWidth = 40.dp, minHeight = 40.dp)
            ) {
                if (!isLoggedIn) {
                    ActivityIndicator(
                        size = 40.dp,
                        fill = false,
                        modifier = Modifier
                            .alpha(spinnerAlpha)
                    )
                } else if (isLocked) {
                    Text(Localization.string("Unlock app"),
                        style = poppinsSemibold(16),
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .heightIn(min = 34.dp)
                            .wrapContentSize(Alignment.Center)
                            .clickable {
                                authenticate()
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Privacy by Default",
                style = satoshiBold(18),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.padding(bottom = 40.dp + systemNavigationBarBottomInset),
            )
        }
    }
}
