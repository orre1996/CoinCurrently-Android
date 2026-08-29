package com.cryptium.coincurrently.ui.main.Compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.General.AppColorScheme
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys

@Composable
fun Separator(
    padding: PaddingValues = PaddingValues(0.dp),
    forceRedraw: Boolean = false,
    alpha: Float = 1f
) {
    Divider(
        color = MaterialTheme.colors.outlineVariant,
        modifier = Modifier
            .padding(padding)
            .height(1.dp)
            .fillMaxWidth()
            .zIndex(if(forceRedraw) 1f else 1f)
            .alpha(alpha)
    )
}

@Composable
fun SeparatorVertical(
    padding: PaddingValues = PaddingValues(0.dp),
    height: Modifier = Modifier,
    forceRedraw: Boolean = false,
    alpha: Float = 1f,
    width: Dp = 1.dp,
    color: Color = MaterialTheme.colors.outlineVariant
) {
    Divider(
        color = color,
        modifier = height
            .padding(padding)
            .width(width)
            .fillMaxHeight()
            .zIndex(if(forceRedraw) 1f else 1f)
            .alpha(alpha)
    )
}


@Composable
fun SeparatorVerticalColor(padding: PaddingValues = PaddingValues(0.dp), height: Modifier, width: Dp = 2.dp, color: Color) {
    Divider(
        color = color,
        modifier = height
            .padding(padding)
            .width(width)
            .fillMaxHeight()
    )
}

@Composable
fun SeparatorDropDown() {
    val isSystemDark = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val isDark = (StorageManager.get<AppColorScheme>(StorageKeys.APP_COLOR_SCHEME) ?: AppColorScheme.DARK).resolvedColorScheme(isSystemDark)
    Divider(
        color = if (isDark) Color.LightGray else Color.DarkGray,
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
            .alpha(0.5f)
    )
}