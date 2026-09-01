package com.cryptium.coincurrently.features.NFTDetail.Views.Overview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTDetail
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts

@Composable
fun NFTDetailOverviewView(
    nftDetailModel: NFTDetail?,
    currentCurrency: CryptoDetailInfoModel?,
    prices: List<Double>?,
    timeChart: TimeCharts,
    selectedCurrency: CurrencySelectionModel,
    chartCurrency: CurrencySelectionModel,
    isFiatSelected: Boolean,
    multiplier: Double?,
    showCurrencySelector: MutableState<Boolean>
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
        NFTDetailStatisticsView(
            nftDetailInfo = nftDetailModel,
            timeChart = timeChart,
            prices = prices,
            chartCurrency = chartCurrency,
            isFiatSelected = isFiatSelected,
            multiplier = multiplier,
        )

        NFTDetailHistoricDataView(
            nftDetail = nftDetailModel,
        )

        NFTDetailCurrencyConverterView(
            currentCurrency = currentCurrency,
            selectedCurrency = selectedCurrency,
            showCurrencySelector = showCurrencySelector,
        )
    }
}