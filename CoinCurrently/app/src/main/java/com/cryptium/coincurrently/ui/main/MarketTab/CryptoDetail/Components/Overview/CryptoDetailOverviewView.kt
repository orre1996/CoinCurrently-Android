package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import networking.ResponseModels.AlertCoin
import networking.ResponseModels.CryptoDetailInfoModel
import networking.ResponseModels.CurrencySelectionModel
import networking.ResponseModels.TimeCharts

@Composable
fun CryptoDetailOverviewView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
    prices: List<Double>?,
    totalMarketCap: Double?,
    timeCharts: TimeCharts,
    alerts: List<AlertCoin>?,
    selectedCurrency: CurrencySelectionModel,
    showCurrencySelector: MutableState<Boolean>,
    showSentimentInfo: MutableState<Boolean>,
    addNewAlert: () -> Unit,
    deleteAlert: (AlertCoin) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(40.dp),
        modifier = Modifier
            .padding(
                start = 16.dp,
                top = 32.dp,
                end = 16.dp
            )
    ) {
        CryptoDetailStatisticsView(
            cryptoDetailInfoModel = cryptoDetailInfoModel,
            prices = prices,
            totalMarketCap = totalMarketCap,
            timeChart = timeCharts,
        )

        CryptoDetailHistoricDataView(
            cryptoDetailInfoModel = cryptoDetailInfoModel,
        )

        CryptoDetailSentimentView(
            sentimentVotesUpPercentage = cryptoDetailInfoModel?.sentiment_votes_up_percentage,
            sentimentVotesDownPercentage = cryptoDetailInfoModel?.sentiment_votes_down_percentage,
            showSentimentInfo = showSentimentInfo,
        )

        CryptoDetailCurrencyConverterView(
            cryptoDetailInfoModel = cryptoDetailInfoModel,
            selectedCurrency = selectedCurrency,
            showCurrencySelector = showCurrencySelector,
        )

        CryptoDetailAlertsView(
            alerts = alerts,
            coinName = cryptoDetailInfoModel?.name,
            addNewAlert = addNewAlert,
            deleteAlert = deleteAlert,
        )
    }
}