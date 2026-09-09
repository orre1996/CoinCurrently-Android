package com.cryptium.coincurrently.widgets.receivers

import com.cryptium.coincurrently.core.extensions.getAllTransactions
import com.cryptium.coincurrently.core.extensions.getAmount
import com.cryptium.coincurrently.core.extensions.getProfitLossPercent
import com.cryptium.coincurrently.core.extensions.getProfitLossValue
import com.cryptium.coincurrently.core.extensions.getProfitPercent
import com.cryptium.coincurrently.core.extensions.getProfitValue
import com.cryptium.coincurrently.core.networking.ResponseModels.Pie
import com.cryptium.coincurrently.core.networking.ResponseModels.shouldShow

internal data class CoinDailyChange(
    val coinId: String,
    val symbol: String,
    val imageUrl: String?,
    val changeValue: Double?,
    val changePercent: Double?,
)

internal data class PortfolioWidgetData(
    val totalValue: Double?,
    val portfolioDailyChangeValue: Double?,
    val portfolioDailyChangePercent: Double?,
    val profitLossValue: Double?,
    val profitLossPercent: Double?,
    val dailyWinner: CoinDailyChange?,
    val dailyLoser: CoinDailyChange?,
    val topCoinsByProfitLoss: List<CoinDailyChange>,
    val isEmpty: Boolean,
)

private data class CoinMetrics(
    val pie: Pie,
    val coinId: String,
    val holdingsValue: Double,
    val changePercent: Double?,
    val changeValue: Double?,
    val profitValue: Double?,
    val profitPercent: Double?,
)

internal fun buildPortfolioWidgetData(
    portfolio: List<Pie>,
    allPrices: Map<String, Map<String, Double>>?,
    currencyCode: String,
): PortfolioWidgetData {
    val currencyKey = currencyCode.lowercase()
    val eligibleCoins = portfolio.filter { pie ->
        pie.data?.id != null && pie.shouldShow
    }

    if (eligibleCoins.isEmpty()) {
        return PortfolioWidgetData(
            totalValue = null,
            portfolioDailyChangeValue = null,
            portfolioDailyChangePercent = null,
            profitLossValue = null,
            profitLossPercent = null,
            dailyWinner = null,
            dailyLoser = null,
            topCoinsByProfitLoss = emptyList(),
            isEmpty = true,
        )
    }

    val metrics = eligibleCoins.mapNotNull { pie ->
        val coinId = pie.data?.id ?: return@mapNotNull null
        val transactions = portfolio.getAllTransactions(coinId)
        val amount = transactions.getAmount()
        val coinPrices = allPrices?.get(coinId)
        val currentPrice = coinPrices?.get(currencyKey)
            ?: pie.data?.current_price
            ?: 0.0
        val holdingsValue = amount * currentPrice
        val changePercent = pie.data?.price_change_percentage_24h_in_currency
        val changeValue = changePercent?.let { holdingsValue * (it / 100.0) }

        CoinMetrics(
            pie = pie,
            coinId = coinId,
            holdingsValue = holdingsValue,
            changePercent = changePercent,
            changeValue = changeValue,
            profitValue = transactions.getProfitValue(coinPrices, currencyCode),
            profitPercent = transactions.getProfitPercent(coinPrices, currencyCode),
        )
    }

    val totalValue = metrics.sumOf { it.holdingsValue }

    val portfolioDailyChangeValue = metrics
        .mapNotNull { it.changeValue }
        .takeIf { it.isNotEmpty() }
        ?.sum()

    val portfolioDailyChangePercent = run {
        val withPercent = metrics.filter { it.changePercent != null }
        if (withPercent.isEmpty()) {
            return@run null
        }
        val weightedSum = withPercent.sumOf { metric ->
            metric.holdingsValue * metric.changePercent!!
        }
        val coveredValue = withPercent.sumOf { it.holdingsValue }
        if (coveredValue <= 0) {
            return@run null
        }
        val result = weightedSum / coveredValue
        if (result.isNaN() || result.isInfinite()) null else result
    }

    val coinsWithPercent = metrics.filter { it.changePercent != null }
    val winnerMetric = coinsWithPercent.maxByOrNull { it.changePercent!! }
    val loserMetric = coinsWithPercent.minByOrNull { it.changePercent!! }
    val topCoinsByProfitLoss = metrics
        .sortedByDescending { it.profitValue ?: Double.NEGATIVE_INFINITY }
        .take(6)
        .map { it.toCoinProfitLoss() }

    return PortfolioWidgetData(
        totalValue = totalValue,
        portfolioDailyChangeValue = portfolioDailyChangeValue,
        portfolioDailyChangePercent = portfolioDailyChangePercent,
        profitLossValue = portfolio.getProfitLossValue(allPrices, currencyCode),
        profitLossPercent = portfolio.getProfitLossPercent(allPrices, currencyCode),
        dailyWinner = winnerMetric?.toCoinDailyChange(),
        dailyLoser = loserMetric?.toCoinDailyChange(),
        topCoinsByProfitLoss = topCoinsByProfitLoss,
        isEmpty = false,
    )
}

private fun CoinMetrics.toCoinDailyChange(): CoinDailyChange {
    return CoinDailyChange(
        coinId = coinId,
        symbol = pie.data?.symbol?.uppercase().orEmpty(),
        imageUrl = pie.data?.image,
        changeValue = changeValue,
        changePercent = changePercent,
    )
}

private fun CoinMetrics.toCoinProfitLoss(): CoinDailyChange {
    return CoinDailyChange(
        coinId = coinId,
        symbol = pie.data?.symbol?.uppercase().orEmpty(),
        imageUrl = pie.data?.image,
        changeValue = profitValue,
        changePercent = profitPercent,
    )
}
