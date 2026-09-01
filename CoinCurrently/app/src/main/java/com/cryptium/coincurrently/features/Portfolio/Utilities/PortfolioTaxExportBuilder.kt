package com.cryptium.coincurrently.features.Portfolio.Utilities
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.features.Portfolio.Models.NewSwapTransaction
import com.cryptium.coincurrently.core.extensions.toCoinTrackerExportDateTimeUTC
import com.cryptium.coincurrently.core.extensions.toPortfolioExportDateTimeUTC
import com.cryptium.coincurrently.features.Portfolio.DataClasses.NewTransaction
import com.cryptium.coincurrently.features.Portfolio.Enums.NewTransactionType
import com.cryptium.coincurrently.core.networking.ResponseModels.Pie
import com.cryptium.coincurrently.core.networking.ResponseModels.sanitizeGsonNullLists
import java.util.Date

object PortfolioTaxExportBuilder {
    private data class ExportEntry(
        val date: Date,
        val row: String,
    )

    fun koinlyCSV(portfolio: List<Pie>): String {
        val header = "Date,Sent Amount,Sent Currency,Received Amount,Received Currency,Fee Amount,Fee Currency,Description"
        return buildCSV(
            header = header,
            portfolio = portfolio,
            transactionRow = { coin, transaction -> koinlyRow(coin, transaction) },
            swapRow = { swap -> koinlySwapRow(swap) },
            importRow = { coin, importEvent -> koinlyImportRow(coin, importEvent) },
        )
    }

    fun coinTrackerCSV(portfolio: List<Pie>): String {
        val header = "Date,Received Quantity,Received Currency,Sent Quantity,Sent Currency,Fee Amount,Fee Currency,Tag"
        return buildCSV(
            header = header,
            portfolio = portfolio,
            transactionRow = { coin, transaction -> coinTrackerRow(coin, transaction) },
            swapRow = { swap -> coinTrackerSwapRow(swap) },
            importRow = { coin, importEvent -> coinTrackerImportRow(coin, importEvent) },
        )
    }

    fun tokenTaxCSV(portfolio: List<Pie>): String {
        val header = "Type,BuyAmount,BuyCurrency,SellAmount,SellCurrency,FeeAmount,FeeCurrency,Exchange,Group,Comment,Date"
        return buildCSV(
            header = header,
            portfolio = portfolio,
            transactionRow = { coin, transaction -> tokenTaxRow(coin, transaction) },
            swapRow = { swap -> tokenTaxSwapRow(swap) },
            importRow = { coin, importEvent -> tokenTaxImportRow(coin, importEvent) },
        )
    }

    fun zenLedgerCSV(portfolio: List<Pie>): String {
        val header = "Timestamp,Type,IN Amount,IN Currency,OUT Amount,OUT Currency,Fee Amount,Fee Currency,Exchange,US Based"
        return buildCSV(
            header = header,
            portfolio = portfolio,
            transactionRow = { coin, transaction -> zenLedgerRow(coin, transaction) },
            swapRow = { swap -> zenLedgerSwapRow(swap) },
            importRow = { coin, importEvent -> zenLedgerImportRow(coin, importEvent) },
        )
    }

    private fun buildCSV(
        header: String,
        portfolio: List<Pie>,
        transactionRow: (String, NewTransaction) -> String,
        swapRow: (NewSwapTransaction) -> String,
        importRow: (String, AddressImport) -> String,
    ): String {
        val entries = mutableListOf<ExportEntry>()

        for (pie in portfolio) {
            pie.sanitizeGsonNullLists()
            val coin = coinSymbol(pie)

            pie.transactions.forEach { transaction ->
                entries.add(ExportEntry(transaction.date, transactionRow(coin, transaction)))
            }

            pie.swaps.forEach { swap ->
                entries.add(ExportEntry(swap.date, swapRow(swap)))
            }

            pie.imports.forEach { importEvent ->
                entries.add(ExportEntry(importEvent.date, importRow(coin, importEvent)))
            }
        }

        val rows = entries
            .sortedByDescending { it.date }
            .map { it.row }

        return (listOf(header) + rows).joinToString("\n")
    }

    private fun coinSymbol(pie: Pie): String {
        return pie.data?.symbol?.uppercase(AppLocale.enUsPosix) ?: pie.data?.name ?: ""
    }

    private fun tradeValue(amount: Double, pricePerCoin: Double?): Double {
        return amount * (pricePerCoin ?: 0.0)
    }

    private fun parsedBalance(balance: String): Double? {
        val normalized = balance.replace(",", "").trim()
        return normalized.toDoubleOrNull()
    }

    private fun csvField(value: String): String {
        return value.replace(",", ".")
    }

    private fun csvNumericField(value: Double?): String {
        return value?.let { csvField(it.toString()) } ?: ""
    }

    private fun feeFields(amount: Double, currency: String): Pair<String, String> {
        if (amount <= 0) return "" to ""
        return csvNumericField(amount) to csvField(currency.uppercase(AppLocale.enUsPosix))
    }

    // MARK: - Koinly

    private fun koinlyRow(coin: String, transaction: NewTransaction): String {
        val fees = feeFields(transaction.fee, transaction.currency)
        val value = tradeValue(transaction.amount, transaction.pricePerCoin)

        return when (transaction.transactionType) {
            NewTransactionType.Buy -> koinlyColumns(
                date = transaction.date.toPortfolioExportDateTimeUTC(),
                sentAmount = csvNumericField(value),
                sentCurrency = csvField(transaction.currency.uppercase(AppLocale.enUsPosix)),
                receivedAmount = csvNumericField(transaction.amount),
                receivedCurrency = csvField(coin),
                feeAmount = fees.first,
                feeCurrency = fees.second,
                description = csvField(transaction.note),
            )
            NewTransactionType.Sell -> koinlyColumns(
                date = transaction.date.toPortfolioExportDateTimeUTC(),
                sentAmount = csvNumericField(transaction.amount),
                sentCurrency = csvField(coin),
                receivedAmount = csvNumericField(value),
                receivedCurrency = csvField(transaction.currency.uppercase(AppLocale.enUsPosix)),
                feeAmount = fees.first,
                feeCurrency = fees.second,
                description = csvField(transaction.note),
            )
            NewTransactionType.TransferIn, NewTransactionType.ImportAddress -> koinlyColumns(
                date = transaction.date.toPortfolioExportDateTimeUTC(),
                sentAmount = "",
                sentCurrency = "",
                receivedAmount = csvNumericField(transaction.amount),
                receivedCurrency = csvField(coin),
                feeAmount = "",
                feeCurrency = "",
                description = csvField(transaction.note),
            )
            NewTransactionType.TransferOut -> koinlyColumns(
                date = transaction.date.toPortfolioExportDateTimeUTC(),
                sentAmount = csvNumericField(transaction.amount),
                sentCurrency = csvField(coin),
                receivedAmount = "",
                receivedCurrency = "",
                feeAmount = "",
                feeCurrency = "",
                description = csvField(transaction.note),
            )
            NewTransactionType.Swap -> koinlyColumns(
                date = transaction.date.toPortfolioExportDateTimeUTC(),
                sentAmount = "",
                sentCurrency = "",
                receivedAmount = "",
                receivedCurrency = "",
                feeAmount = "",
                feeCurrency = "",
                description = csvField(transaction.note),
            )
        }
    }

    private fun koinlySwapRow(swap: NewSwapTransaction): String {
        val fees = feeFields(swap.fee, swap.selectedCurrency)

        return koinlyColumns(
            date = swap.date.toPortfolioExportDateTimeUTC(),
            sentAmount = csvNumericField(swap.fromAmount),
            sentCurrency = csvField(swap.fromCurrencySymbol.uppercase(AppLocale.enUsPosix)),
            receivedAmount = csvNumericField(swap.toAmount),
            receivedCurrency = csvField(swap.toCurrencySymbol.uppercase(AppLocale.enUsPosix)),
            feeAmount = fees.first,
            feeCurrency = fees.second,
            description = csvField(swap.note),
        )
    }

    private fun koinlyImportRow(coin: String, importEvent: AddressImport): String {
        val amount = parsedBalance(importEvent.balance)
        val description = listOfNotNull(importEvent.walletName?.trim(), importEvent.address.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" · ")

        return koinlyColumns(
            date = importEvent.date.toPortfolioExportDateTimeUTC(),
            sentAmount = "",
            sentCurrency = "",
            receivedAmount = csvNumericField(amount),
            receivedCurrency = csvField(coin),
            feeAmount = "",
            feeCurrency = "",
            description = csvField(description),
        )
    }

    private fun koinlyColumns(
        date: String,
        sentAmount: String,
        sentCurrency: String,
        receivedAmount: String,
        receivedCurrency: String,
        feeAmount: String,
        feeCurrency: String,
        description: String,
    ): String {
        return listOf(
            date, sentAmount, sentCurrency, receivedAmount, receivedCurrency,
            feeAmount, feeCurrency, description,
        ).joinToString(",")
    }

    // MARK: - CoinTracker

    private fun coinTrackerRow(coin: String, transaction: NewTransaction): String {
        val fees = feeFields(transaction.fee, transaction.currency)
        val value = tradeValue(transaction.amount, transaction.pricePerCoin)

        return when (transaction.transactionType) {
            NewTransactionType.Buy -> coinTrackerColumns(
                date = transaction.date.toCoinTrackerExportDateTimeUTC(),
                receivedQuantity = csvNumericField(transaction.amount),
                receivedCurrency = csvField(coin),
                sentQuantity = csvNumericField(value),
                sentCurrency = csvField(transaction.currency.uppercase(AppLocale.enUsPosix)),
                feeAmount = fees.first,
                feeCurrency = fees.second,
                tag = "",
            )
            NewTransactionType.Sell -> coinTrackerColumns(
                date = transaction.date.toCoinTrackerExportDateTimeUTC(),
                receivedQuantity = csvNumericField(value),
                receivedCurrency = csvField(transaction.currency.uppercase(AppLocale.enUsPosix)),
                sentQuantity = csvNumericField(transaction.amount),
                sentCurrency = csvField(coin),
                feeAmount = fees.first,
                feeCurrency = fees.second,
                tag = "",
            )
            NewTransactionType.TransferIn, NewTransactionType.ImportAddress -> coinTrackerColumns(
                date = transaction.date.toCoinTrackerExportDateTimeUTC(),
                receivedQuantity = csvNumericField(transaction.amount),
                receivedCurrency = csvField(coin),
                sentQuantity = "",
                sentCurrency = "",
                feeAmount = "",
                feeCurrency = "",
                tag = "",
            )
            NewTransactionType.TransferOut -> coinTrackerColumns(
                date = transaction.date.toCoinTrackerExportDateTimeUTC(),
                receivedQuantity = "",
                receivedCurrency = "",
                sentQuantity = csvNumericField(transaction.amount),
                sentCurrency = csvField(coin),
                feeAmount = "",
                feeCurrency = "",
                tag = "",
            )
            NewTransactionType.Swap -> coinTrackerColumns(
                date = transaction.date.toCoinTrackerExportDateTimeUTC(),
                receivedQuantity = "",
                receivedCurrency = "",
                sentQuantity = "",
                sentCurrency = "",
                feeAmount = "",
                feeCurrency = "",
                tag = "",
            )
        }
    }

    private fun coinTrackerSwapRow(swap: NewSwapTransaction): String {
        val fees = feeFields(swap.fee, swap.selectedCurrency)

        return coinTrackerColumns(
            date = swap.date.toCoinTrackerExportDateTimeUTC(),
            receivedQuantity = csvNumericField(swap.toAmount),
            receivedCurrency = csvField(swap.toCurrencySymbol.uppercase(AppLocale.enUsPosix)),
            sentQuantity = csvNumericField(swap.fromAmount),
            sentCurrency = csvField(swap.fromCurrencySymbol.uppercase(AppLocale.enUsPosix)),
            feeAmount = fees.first,
            feeCurrency = fees.second,
            tag = "",
        )
    }

    private fun coinTrackerImportRow(coin: String, importEvent: AddressImport): String {
        return coinTrackerColumns(
            date = importEvent.date.toCoinTrackerExportDateTimeUTC(),
            receivedQuantity = csvNumericField(parsedBalance(importEvent.balance)),
            receivedCurrency = csvField(coin),
            sentQuantity = "",
            sentCurrency = "",
            feeAmount = "",
            feeCurrency = "",
            tag = "",
        )
    }

    private fun coinTrackerColumns(
        date: String,
        receivedQuantity: String,
        receivedCurrency: String,
        sentQuantity: String,
        sentCurrency: String,
        feeAmount: String,
        feeCurrency: String,
        tag: String,
    ): String {
        return listOf(
            date, receivedQuantity, receivedCurrency, sentQuantity, sentCurrency,
            feeAmount, feeCurrency, tag,
        ).joinToString(",")
    }

    // MARK: - TokenTax

    private fun tokenTaxRow(coin: String, transaction: NewTransaction): String {
        val fees = feeFields(transaction.fee, transaction.currency)
        val value = tradeValue(transaction.amount, transaction.pricePerCoin)

        return when (transaction.transactionType) {
            NewTransactionType.Buy -> tokenTaxColumns(
                type = "Trade",
                buyAmount = csvNumericField(transaction.amount),
                buyCurrency = csvField(coin),
                sellAmount = csvNumericField(value),
                sellCurrency = csvField(transaction.currency.uppercase(AppLocale.enUsPosix)),
                feeAmount = fees.first,
                feeCurrency = fees.second,
                comment = csvField(transaction.note),
                date = transaction.date.toPortfolioExportDateTimeUTC(),
            )
            NewTransactionType.Sell -> tokenTaxColumns(
                type = "Trade",
                buyAmount = csvNumericField(value),
                buyCurrency = csvField(transaction.currency.uppercase(AppLocale.enUsPosix)),
                sellAmount = csvNumericField(transaction.amount),
                sellCurrency = csvField(coin),
                feeAmount = fees.first,
                feeCurrency = fees.second,
                comment = csvField(transaction.note),
                date = transaction.date.toPortfolioExportDateTimeUTC(),
            )
            NewTransactionType.TransferIn, NewTransactionType.ImportAddress -> tokenTaxColumns(
                type = "Income",
                buyAmount = csvNumericField(transaction.amount),
                buyCurrency = csvField(coin),
                sellAmount = "",
                sellCurrency = "",
                feeAmount = "",
                feeCurrency = "",
                comment = csvField(transaction.note),
                date = transaction.date.toPortfolioExportDateTimeUTC(),
            )
            NewTransactionType.TransferOut -> tokenTaxColumns(
                type = "Spending",
                buyAmount = "",
                buyCurrency = "",
                sellAmount = csvNumericField(transaction.amount),
                sellCurrency = csvField(coin),
                feeAmount = "",
                feeCurrency = "",
                comment = csvField(transaction.note),
                date = transaction.date.toPortfolioExportDateTimeUTC(),
            )
            NewTransactionType.Swap -> tokenTaxColumns(
                type = "Trade",
                buyAmount = "",
                buyCurrency = "",
                sellAmount = "",
                sellCurrency = "",
                feeAmount = "",
                feeCurrency = "",
                comment = csvField(transaction.note),
                date = transaction.date.toPortfolioExportDateTimeUTC(),
            )
        }
    }

    private fun tokenTaxSwapRow(swap: NewSwapTransaction): String {
        val fees = feeFields(swap.fee, swap.selectedCurrency)

        return tokenTaxColumns(
            type = "Trade",
            buyAmount = csvNumericField(swap.toAmount),
            buyCurrency = csvField(swap.toCurrencySymbol.uppercase(AppLocale.enUsPosix)),
            sellAmount = csvNumericField(swap.fromAmount),
            sellCurrency = csvField(swap.fromCurrencySymbol.uppercase(AppLocale.enUsPosix)),
            feeAmount = fees.first,
            feeCurrency = fees.second,
            comment = csvField(swap.note),
            date = swap.date.toPortfolioExportDateTimeUTC(),
        )
    }

    private fun tokenTaxImportRow(coin: String, importEvent: AddressImport): String {
        val description = listOfNotNull(importEvent.walletName?.trim(), importEvent.address.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" · ")

        return tokenTaxColumns(
            type = "Income",
            buyAmount = csvNumericField(parsedBalance(importEvent.balance)),
            buyCurrency = csvField(coin),
            sellAmount = "",
            sellCurrency = "",
            feeAmount = "",
            feeCurrency = "",
            comment = csvField(description),
            date = importEvent.date.toPortfolioExportDateTimeUTC(),
        )
    }

    private fun tokenTaxColumns(
        type: String,
        buyAmount: String,
        buyCurrency: String,
        sellAmount: String,
        sellCurrency: String,
        feeAmount: String,
        feeCurrency: String,
        comment: String,
        date: String,
    ): String {
        return listOf(
            type, buyAmount, buyCurrency, sellAmount, sellCurrency,
            feeAmount, feeCurrency, "CoinCurrently", "", comment, date,
        ).joinToString(",")
    }

    // MARK: - ZenLedger

    private fun zenLedgerRow(coin: String, transaction: NewTransaction): String {
        val fees = feeFields(transaction.fee, transaction.currency)
        val value = tradeValue(transaction.amount, transaction.pricePerCoin)

        return when (transaction.transactionType) {
            NewTransactionType.Buy -> zenLedgerColumns(
                timestamp = transaction.date.toPortfolioExportDateTimeUTC(),
                type = "buy",
                inAmount = csvNumericField(transaction.amount),
                inCurrency = csvField(coin),
                outAmount = csvNumericField(value),
                outCurrency = csvField(transaction.currency.uppercase(AppLocale.enUsPosix)),
                feeAmount = fees.first,
                feeCurrency = fees.second,
            )
            NewTransactionType.Sell -> zenLedgerColumns(
                timestamp = transaction.date.toPortfolioExportDateTimeUTC(),
                type = "sell",
                inAmount = csvNumericField(value),
                inCurrency = csvField(transaction.currency.uppercase(AppLocale.enUsPosix)),
                outAmount = csvNumericField(transaction.amount),
                outCurrency = csvField(coin),
                feeAmount = fees.first,
                feeCurrency = fees.second,
            )
            NewTransactionType.TransferIn, NewTransactionType.ImportAddress -> zenLedgerColumns(
                timestamp = transaction.date.toPortfolioExportDateTimeUTC(),
                type = "receive",
                inAmount = csvNumericField(transaction.amount),
                inCurrency = csvField(coin),
                outAmount = "",
                outCurrency = "",
                feeAmount = "",
                feeCurrency = "",
            )
            NewTransactionType.TransferOut -> zenLedgerColumns(
                timestamp = transaction.date.toPortfolioExportDateTimeUTC(),
                type = "send",
                inAmount = "",
                inCurrency = "",
                outAmount = csvNumericField(transaction.amount),
                outCurrency = csvField(coin),
                feeAmount = "",
                feeCurrency = "",
            )
            NewTransactionType.Swap -> zenLedgerColumns(
                timestamp = transaction.date.toPortfolioExportDateTimeUTC(),
                type = "trade",
                inAmount = "",
                inCurrency = "",
                outAmount = "",
                outCurrency = "",
                feeAmount = "",
                feeCurrency = "",
            )
        }
    }

    private fun zenLedgerSwapRow(swap: NewSwapTransaction): String {
        val fees = feeFields(swap.fee, swap.selectedCurrency)

        return zenLedgerColumns(
            timestamp = swap.date.toPortfolioExportDateTimeUTC(),
            type = "trade",
            inAmount = csvNumericField(swap.toAmount),
            inCurrency = csvField(swap.toCurrencySymbol.uppercase(AppLocale.enUsPosix)),
            outAmount = csvNumericField(swap.fromAmount),
            outCurrency = csvField(swap.fromCurrencySymbol.uppercase(AppLocale.enUsPosix)),
            feeAmount = fees.first,
            feeCurrency = fees.second,
        )
    }

    private fun zenLedgerImportRow(coin: String, importEvent: AddressImport): String {
        return zenLedgerColumns(
            timestamp = importEvent.date.toPortfolioExportDateTimeUTC(),
            type = "receive",
            inAmount = csvNumericField(parsedBalance(importEvent.balance)),
            inCurrency = csvField(coin),
            outAmount = "",
            outCurrency = "",
            feeAmount = "",
            feeCurrency = "",
        )
    }

    private fun zenLedgerColumns(
        timestamp: String,
        type: String,
        inAmount: String,
        inCurrency: String,
        outAmount: String,
        outCurrency: String,
        feeAmount: String,
        feeCurrency: String,
    ): String {
        return listOf(
            timestamp, type, inAmount, inCurrency, outAmount, outCurrency,
            feeAmount, feeCurrency, "CoinCurrently", "",
        ).joinToString(",")
    }
}
