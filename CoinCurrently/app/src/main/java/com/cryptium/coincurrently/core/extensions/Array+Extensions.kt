package com.cryptium.coincurrently.core.extensions
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.features.Portfolio.Models.PortfolioSwapTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioModel
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import com.cryptium.coincurrently.core.networking.ResponseModels.Pie
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.sanitizeGsonNullLists
import java.util.Calendar
import java.util.Date
import kotlin.text.lowercase


fun normalise(inValue: Float, min: Float, max: Float): Float {
    return (inValue - min) / (max - min)
}


fun List<Double>.normalize(): List<Float> {
    if (this.all { it == this.firstOrNull() }) {
        return this.map { 0.5f }
    }

    val min = this.minOrNull() ?: return emptyList()
    val max = this.maxOrNull() ?: return emptyList()

    if (max == min) {
        return this.map { 0.5f }
    }

    return this.map { (it.toFloat() - min.toFloat()) / (max.toFloat() - min.toFloat()) }
}

fun List<Double>.normalizeCustom(min: Double, max: Double): List<Float> {
    val range = (max - min).toFloat()
    if (range == 0f) {
        return this.map { if (it == -1.0) -1f else 0.5f }
    }
    return this.map { price ->
        // Preserve compare-chart padding sentinels (same as iOS NewChartView).
        if (price == -1.0) -1f else (price.toFloat() - min.toFloat()) / range
    }
}

fun List<PortfolioTransaction>.ofType(type: PortfolioTransactionType): List<PortfolioTransaction> {
    return this.filter { it.transactionType == type }
}

private fun List<PortfolioTransaction>.filtered(
    type: PortfolioTransactionType? = null,
    withTransfers: Boolean = true,
    after: Date = Date(0), // distantPast
    before: Date = Date()
): List<PortfolioTransaction> {
    return this.filter { tx ->
        type?.let {
            if (tx.transactionType != it) return@filter false
            if (!withTransfers && (it.isTransfer || it == PortfolioTransactionType.ImportAddress)) return@filter false
        }

        if (tx.date.before(after) || tx.date.after(before)) return@filter false
        true
    }
}

fun List<PortfolioTransaction>.sumAmounts(
    type: PortfolioTransactionType,
    withTransfers: Boolean = true,
    after: Date = Date(0), // distantPast
    before: Date = Date()
): Double {
    return this.filtered(type, withTransfers, after, before)
        .sumOf { it.amount }
}

fun List<PortfolioTransaction>.getAmount(
    withTransfers: Boolean = true,
    after: Date = Date(0), // distantPast
    before: Date = Date()
): Double {
    return this.sumAmounts(PortfolioTransactionType.ImportAddress, withTransfers, after, before) +
        this.sumAmounts(PortfolioTransactionType.Buy, true, after, before) +
        this.sumAmounts(PortfolioTransactionType.TransferIn, withTransfers, after, before) -
        this.sumAmounts(PortfolioTransactionType.Sell, true, after, before) -
        this.sumAmounts(PortfolioTransactionType.TransferOut, withTransfers, after, before)
}

fun List<PortfolioTransaction>.afterDate(date: Date): List<PortfolioTransaction> {
    return this.filtered(after = date)
}

fun List<PortfolioTransaction>.beforeDate(date: Date): List<PortfolioTransaction> {
    return this.filtered(before = date)
}

val List<PortfolioModel>.defaultWalletName: String
    get() {
        @Suppress("UNCHECKED_CAST")
        val imports = sumOf { ((it.imports as List<AddressImport>?) ?: emptyList()).count() }
        return if(imports == 0) "Wallet" else "Wallet $imports"
    }
val List<PortfolioModel>.allIds: List<String>
    get() {
        @Suppress("UNCHECKED_CAST")
        return map { it.id } + flatMap {
            (it.imports as List<AddressImport>?) ?: emptyList()
        }
            .flatMap { it.tokenInfos ?: listOf() }
            .mapNotNull { it.tokenId }
        }

fun List<Pie>.pieContaining(event: PortfolioEvent): Pie? {
    val id = event.id
    return when (event) {
        is PortfolioTransaction -> firstOrNull { pie ->
            pie.sanitizeGsonNullLists().transactions.any { it.id == id }
        }
        is PortfolioSwapTransaction -> firstOrNull { pie ->
            pie.sanitizeGsonNullLists().swaps.any { it.id == id }
        }
        else -> firstOrNull { pie ->
            pie.sanitizeGsonNullLists().imports.any { it.id == id }
        }
    }
}

fun List<Pie>.groupedTransactions(
    types: List<PortfolioTransactionType>,
    dateOption: DateOption,
    coinNames: List<Toplist>
): Map<Date, List<PortfolioEvent>> {

    val result = mutableMapOf<Date, MutableList<PortfolioEvent>>()
    val calendar = Calendar.getInstance()
    val now = Date()

    // 1) Resolve date range
    val range: ClosedRange<Date>? = when (dateOption) {
        DateOption.All -> null

        DateOption.LastWeek ->
            calendar.addAndReturn(now, Calendar.DAY_OF_YEAR, -7)..now

        DateOption.LastMonth ->
            calendar.addAndReturn(now, Calendar.MONTH, -1)..now

        DateOption.LastThreeMonths ->
            calendar.addAndReturn(now, Calendar.MONTH, -3)..now

        DateOption.LastSixMonths ->
            calendar.addAndReturn(now, Calendar.MONTH, -6)..now

        DateOption.LastYear ->
            calendar.addAndReturn(now, Calendar.YEAR, -1)..now

        DateOption.YearToDate -> {
            val start = calendar.apply {
                time = now
                set(Calendar.MONTH, 0)
                set(Calendar.DAY_OF_MONTH, 1)
            }.time
            start..now
        }

        is DateOption.Custom -> {
            val start = dateOption.startDate
            val end = dateOption.endDate
            if (start != null && end != null) start..end else null
        }
    }

    for (pie in this) {
        pie.sanitizeGsonNullLists()

        // ✅ Coin filter
        if (coinNames.isNotEmpty()) {
            val matches = coinNames.any { name ->
                pie.data?.symbol?.contains(name.symbol ?: "", true) == true ||
                        pie.data?.name?.contains(name.name ?: "", true) == true ||
                        pie.data?.id?.contains(name.id ?: "", true) == true
            }

            if (!matches) continue
        }

        // Transactions
        for (tx in pie.transactions) {
            if (types.isNotEmpty() &&
                !types.contains(tx.transactionType)
            ) continue

            if (!isInRange(tx.date, range)) continue

            val day = calendar.startOfDay(tx.date)
            result.getOrPut(day) { mutableListOf() }.add(tx)
        }

        // Swaps
        for (swap in pie.swaps) {
            if (types.isNotEmpty() &&
                !types.contains(PortfolioTransactionType.Swap)
            ) continue

            if (!isInRange(swap.date, range)) continue

            val day = calendar.startOfDay(swap.date)
            result.getOrPut(day) { mutableListOf() }.add(swap)
        }

        // Imports
        for (importEvent in pie.imports) {
            if (types.isNotEmpty() &&
                !types.contains(PortfolioTransactionType.ImportAddress)
            ) continue

            if (!isInRange(importEvent.date, range)) continue

            val day = calendar.startOfDay(importEvent.date)
            result.getOrPut(day) { mutableListOf() }.add(importEvent)
        }
    }

    // Sort newest first
    result.forEach { (_, list) ->
        list.sortByDescending { it.date }
    }

    return result
}

private fun isInRange(
    date: Date,
    range: ClosedRange<Date>?
): Boolean {
    return range?.contains(date) ?: true
}

private fun Calendar.addAndReturn(
    base: Date,
    field: Int,
    amount: Int
): Date {
    time = base
    add(field, amount)
    return time
}

private fun Calendar.startOfDay(date: Date): Date {
    time = date
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return time
}


fun List<Pie>.getAllTransactions(id: String?): List<PortfolioTransaction> {
    if (id == null) return emptyList()

    forEach { it.sanitizeGsonNullLists() }

    val coins = filter { it.data?.id == id }

    val transactions = coins.flatMap { it.transactions }

    val buySwapTransactions = coins
        .flatMap { it.swaps }
        .map { it.buyTransaction }

    val swapSellTransactions = flatMap { it.swaps }
        .filter { it.fromCurrencyId == id }
        .map { it.sellTransaction }

    val importTransactions = coins
        .flatMap { it.imports }
        .mapNotNull { it.importTransaction() }

    val tokenTransactions = flatMap { it.imports }
        .flatMap { importItem ->
            (importItem.tokenInfos ?: emptyList()).map { token ->
                token to importItem.date
            }
        }
        .filter { (token, _) -> token.tokenId == id }
        .mapNotNull { (token, date) ->
            token.toAddressImport(date).importTransaction()
        }

    return transactions + buySwapTransactions + swapSellTransactions + importTransactions + tokenTransactions
}

fun List<Pie>.getProfitLossValue(
    coinPrices: Map<String, Map<String, Double>>?,
    showCurrency: String = UserPreferences.getSelectedCurrency().code
): Double? {
    if (isEmpty() || coinPrices.isNullOrEmpty()) return 0.0

    var profitLoss = 0.0
    var hasEntryPrices = false

    for (pie in this) {
        val transactions = getAllTransactions(pie.data?.id)
        val prices = coinPrices[pie.data?.id]

        val profit = transactions.getProfitValue(prices, showCurrency)
        if (profit != null) {
            profitLoss += profit
            hasEntryPrices = true
        }
    }

    return if (hasEntryPrices) profitLoss else 0.0
}

fun List<Pie>.getProfitLossPercent(
    coinPrices: Map<String, Map<String, Double>>?,
    showCurrency: String = UserPreferences.getSelectedCurrency().code
): Double {
    if (isEmpty() || coinPrices.isNullOrEmpty()) return 0.0

    var totalProfit = 0.0
    var totalSpent = 0.0

    for (pie in this) {
        val transactions = getAllTransactions(pie.data?.id)
        val prices = coinPrices[pie.data?.id]

        if (transactions.getProfitPercent(prices, showCurrency) != null) {
            totalProfit += transactions.getProfitValue(prices, showCurrency, withTransfers = false) ?: 0.0
            totalSpent += transactions.totalBuyAmount * (transactions.getAverageCost(prices, showCurrency) ?: 0.0)
        }
    }

    val percent = totalProfit / totalSpent
    return if (percent.isNaN() || percent.isInfinite()) 0.0 else percent * 100
}

val List<Pie>.allTransactionsCount: Int
    get() = sumOf { pie ->
        pie.sanitizeGsonNullLists()
        pie.transactions.size + pie.swaps.size + pie.imports.size
    }

val List<Pie>.allWallets: List<AddressImport>
    get() = flatMap { it.sanitizeGsonNullLists().imports }

private fun priceMultiplier(
    prices: Map<String, Double>?,
    showCurrency: String,
    transactionCurrency: String
): Double {
    val show = prices?.get(showCurrency.lowercase()) ?: 0.0
    val trans = prices?.get(transactionCurrency.lowercase()) ?: 0.0
    return if (trans != 0.0) show / trans else 0.0
}

private fun safeValue(value: Double): Double? {
    return if (value.isNaN() || value.isInfinite()) null else value
}

fun List<PortfolioTransaction>.getTotalFees(
    prices: Map<String, Double>?,
    showCurrency: String = UserPreferences.getSelectedCurrency().code
): Double? {
    val fees = mapNotNull { tx ->
        tx.pricePerCoin?.let {
            val multiplier = priceMultiplier(prices, showCurrency, tx.currency)
            tx.fee * multiplier
        }
    }.sum()

    return safeValue(fees) ?: 0.0
}

fun List<PortfolioTransaction>.getTotalCost(
    prices: Map<String, Double>?,
    showCurrency: String = UserPreferences.getSelectedCurrency().code
): Double? {
    val total = filter { it.transactionType == PortfolioTransactionType.Buy }
        .mapNotNull { tx ->
            tx.pricePerCoin?.let { price ->
                val multiplier = priceMultiplier(prices, showCurrency, tx.currency)
                (price * tx.amount * multiplier) + tx.fee
            }
        }
        .sum()

    return safeValue(total) ?: 0.0
}

val List<PortfolioTransaction>.totalBuyAmount: Double
    get() = filter { it.transactionType == PortfolioTransactionType.Buy }
        .sumOf { it.amount }

fun List<PortfolioTransaction>.getAverageCost(
    prices: Map<String, Double>?,
    showCurrency: String = UserPreferences.getSelectedCurrency().code.lowercase()
): Double? {
    val totalCost = filter { it.transactionType == PortfolioTransactionType.Buy }
        .sumOf { tx ->
            val multiplier = priceMultiplier(prices, showCurrency, tx.currency)
            tx.amount * ((tx.pricePerCoin ?: 1.0) * multiplier)
        }

    return safeValue(totalCost / totalBuyAmount)
}

fun List<PortfolioTransaction>.getProfitPercent(
    prices: Map<String, Double>?,
    showCurrency: String = UserPreferences.getSelectedCurrency().code.lowercase(),
    withTransfers: Boolean = true
): Double? {
    val avgCost = getAverageCost(prices, showCurrency) ?: 1.0
    val originalCost = totalBuyAmount * avgCost

    val sellCost = filter { it.transactionType == PortfolioTransactionType.Sell }
        .sumOf { tx ->
            val multiplier = priceMultiplier(prices, showCurrency, tx.currency)
            tx.amount * ((tx.pricePerCoin ?: 0.0) * multiplier)
        }

    val currentValue = getAmount(withTransfers) * (prices?.get(showCurrency.lowercase()) ?: 0.0) + sellCost

    return safeValue(((currentValue / originalCost) - 1) * 100)
}

fun List<PortfolioTransaction>.getProfitValue(
    prices: Map<String, Double>?,
    showCurrency: String = UserPreferences.getSelectedCurrency().code,
    withTransfers: Boolean = true
): Double? {
    if (prices.isNullOrEmpty()) return 0.0

    val price = prices[showCurrency.lowercase()] ?: 0.0
    val importValues = sumAmounts(PortfolioTransactionType.ImportAddress, withTransfers) * price
    val inValue = sumAmounts(PortfolioTransactionType.TransferIn, withTransfers) * price
    val outValue = sumAmounts(PortfolioTransactionType.TransferOut, withTransfers) * price

    val profitPercent = getProfitPercent(prices, showCurrency, withTransfers = false) ?: 0.0
    val avgCost = getAverageCost(prices, showCurrency) ?: 0.0

    val profit = (profitPercent / 100) * (avgCost * totalBuyAmount) + importValues + inValue - outValue

    return safeValue(profit)
}