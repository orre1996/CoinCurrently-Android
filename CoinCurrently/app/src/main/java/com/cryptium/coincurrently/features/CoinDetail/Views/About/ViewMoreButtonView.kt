package com.cryptium.coincurrently.features.CoinDetail.Views.About
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainer
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.ViewMoreCollapsed
import com.cryptium.coincurrently.core.analytics.ViewMoreExpanded


@Composable
fun ViewMoreButtonView(
    isExpanded: MutableState<Boolean>,
    fromScreen: ScreenLabel,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = if (isExpanded.value) "View Less" else "View More",
            style = satoshiBold(18),
            color = MaterialTheme.colors.onSurfaceContainer,
            modifier = Modifier
                .semantics {
                    contentDescription = if (isExpanded.value) "View less" else "View more"
                    role = Role.Button
                }
                .clickable {
                    if (isExpanded.value) {
                        AnalyticsManager.log(ViewMoreCollapsed(fromScreen = fromScreen))
                    } else {
                        AnalyticsManager.log(ViewMoreExpanded(fromScreen = fromScreen))
                    }
                    isExpanded.value = !isExpanded.value
                }
                .padding(
                    horizontal = 24.dp,
                    vertical = 12.dp
                )
        )
    }
}
