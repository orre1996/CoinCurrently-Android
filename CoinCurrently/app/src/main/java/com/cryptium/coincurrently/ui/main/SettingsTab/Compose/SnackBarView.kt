package com.cryptium.coincurrently.ui.main.SettingsTab.Compose

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Extensions.poppinsRegular

@Composable
fun SnackBarView(message: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {

                    })
            }
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = Color(249, 206, 227),
                shape = RoundedCornerShape(size = 8.dp)
            )
            .border(
                BorderStroke(
                    1.dp,
                    Color(217, 0, 0)
                ), shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Image(painter = painterResource(id = R.drawable.ic_baseline_close_24),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color(217, 0, 0)),
            modifier = Modifier
                .imageSize(24.dp)
        )

        Text(
            text = message,
            style = poppinsRegular(16),
            color = Color(217, 0, 0),
            modifier = Modifier
                .heightIn(24.dp)
        )
    }
}