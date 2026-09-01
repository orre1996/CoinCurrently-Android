package com.cryptium.coincurrently.features.CoinDetail.Views.Overview
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.components.tabPageTalkBackTraversal
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.sheets.EmptyContent
import com.cryptium.coincurrently.features.CoinDetail.Views.General.CryptoDetailAlertCellView
import com.cryptium.coincurrently.core.networking.ResponseModels.AlertCoin

@Composable
fun CryptoDetailAlertsView(
    alerts: List<AlertCoin>?,
    coinName: String?,
    addNewAlert: () -> Unit,
    deleteAlert: (AlertCoin) -> Unit,
) {
    val tabPage = Modifier.tabPageTalkBackTraversal()
    val isTablet = LocalContext.current.isTablet()
    val placeholderCount = if (isTablet) 6 else 3

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeaderView(
            coinName = coinName,
            addNewAlert = addNewAlert,
            tabPage = tabPage,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            alerts?.let {
                if (it.isEmpty()) {
                    EmptyContent(
                        title = "No price alerts yet",
                        subTitle = "Tap + to create your first alert.",
                        tabPageTalkBack = true,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                } else {
                    RealList(
                        alerts = it,
                        coinName = coinName,
                        tabPage = tabPage,
                        deleteAlert = deleteAlert
                    )
                }
            } ?: run {
                TempList(placeholderCount = placeholderCount, tabPage = tabPage)
            }
        }
    }
}

@Composable
private fun HeaderView(
    coinName: String?,
    addNewAlert: () -> Unit,
    tabPage: Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Alerts",
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = tabPage.semantics { heading() },
        )

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.add),
            contentDescription = "Add new ${coinName ?: "coin"} price alert",
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = tabPage
                .padding(8.dp)
                .imageSize(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    addNewAlert.invoke()
                }
        )
    }
}

@Composable
private fun TempList(placeholderCount: Int, tabPage: Modifier) {
    (0 until placeholderCount).forEach {
        CryptoDetailAlertCellView(
            symbol = null,
            alert = null,
            tabPage = tabPage,
            deleteAlert = { }
        )

        if (it != placeholderCount - 1) {
            Separator()
        }
    }
}

@Composable
private fun RealList(
    alerts: List<AlertCoin>,
    coinName: String?,
    tabPage: Modifier,
    deleteAlert: (AlertCoin) -> Unit
) {
    alerts.forEachIndexed { index, alert ->
        CryptoDetailAlertCellView(
            symbol = null,
            alert = alert,
            name = coinName,
            index = index,
            alertsCount = alerts.size,
            tabPage = tabPage,
            deleteAlert = {
                deleteAlert.invoke(alert)
            }
        )

        if (alert != alerts.last()) {
            Separator()
        }
    }
}