package com.cryptium.coincurrently.features.CoinDetail.Views.Markets
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.features.CoinDetail.CryptoDetailViewModel
import com.cryptium.coincurrently.features.ExchangeDetail.ExchangeDetailViewModel
import com.cryptium.coincurrently.features.NFTDetail.NFTDetailViewModel
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeTicker
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeTickerConverted
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTTicker
import com.cryptium.coincurrently.core.networking.ResponseModels.Ticker
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

private fun ExchangeTickerConverted.getSelectedCurrency(): Double? {
    val code = GlobalFunctions.getSelectedCurrency().code.lowercase()
    return when (code) {
        "btc" -> btc
        "eth" -> eth
        "usd" -> usd
        else -> usd
    }
}

private fun ExchangeTicker.volumeSortKey(): Double =
    converted_volume?.getSelectedCurrency() ?: converted_last?.getSelectedCurrency() ?: 0.0

private fun ExchangeTicker.priceSortKey(): Double =
    converted_last?.getSelectedCurrency() ?: 0.0

private fun ExchangeTicker.pairSortName(): String =
    listOfNotNull(base, target).joinToString("/").lowercase(AppLocale.enUsPosix)

private fun Ticker.volumeSortKey(): Double =
    converted_volume?.getSelectedCurrency() ?: converted_last?.getSelectedCurrency() ?: 0.0

private fun Ticker.priceSortKey(): Double =
    converted_last?.getSelectedCurrency() ?: 0.0

private fun Ticker.cryptoMarketSortName(): String =
    (market?.name ?: "").lowercase(AppLocale.enUsPosix)

private fun NFTTicker.nftFloorSortKey(): Double =
    floor_price_in_native_currency ?: 0.0

private fun NFTTicker.nftVolumeSortKey(): Double =
    h24_volume_in_native_currency ?: 0.0

private fun NFTTicker.nftNameSortKey(): String =
    (name ?: "").lowercase(AppLocale.enUsPosix)

fun CryptoDetailViewModel.toDetailMarketsData(): Pair<DetailMarketsConfig, List<DetailMarketsItem>?> {
    val tickers = cryptoInfo?.tickers
    val sorted: List<Ticker>? = when (selectedSortOption) {
        SortOption.VolumeDesc -> tickers?.sortedByDescending { it.volumeSortKey() }
        SortOption.VolumeAsc -> tickers?.sortedBy { it.volumeSortKey() }
        SortOption.priceDesc -> tickers?.sortedByDescending { it.priceSortKey() }
        SortOption.priceAsc -> tickers?.sortedBy { it.priceSortKey() }
        SortOption.NameDesc -> tickers?.sortedBy { it.cryptoMarketSortName() }
        SortOption.NameAsc -> tickers?.sortedByDescending { it.cryptoMarketSortName() }
        else -> tickers?.sortedByDescending { it.volumeSortKey() }
    }
    val items = sorted?.map { it.toDetailMarketsItem() } ?: return DetailMarketsConfig.Crypto to null
    return DetailMarketsConfig.Crypto to items
}

fun Ticker.toDetailMarketsItem(): DetailMarketsItem {
    val priceUsd = converted_last?.getSelectedCurrency()
    val volumeUsd = converted_volume?.getSelectedCurrency()

    return DetailMarketsItem(
        imageUrl = trade_url,
        name = market?.name,
        price = priceUsd?.formatCurrency(FormattingThreshold.NONE),
        volume = volumeUsd?.formatCurrency(),
        tradeUrl = trade_url
    )
}

fun NFTDetailViewModel.toDetailMarketsData(): Pair<DetailMarketsConfig, List<DetailMarketsItem>?> {
    val tickers = nftTickers
    val sorted: List<NFTTicker>? = when (selectedSortOption) {
        SortOption.FloorPriceDesc -> tickers?.sortedByDescending { it.nftFloorSortKey() }
        SortOption.FloorPriceAsc -> tickers?.sortedBy { it.nftFloorSortKey() }
        SortOption.NameDesc -> tickers?.sortedBy { it.nftNameSortKey() }
        SortOption.NameAsc -> tickers?.sortedByDescending { it.nftNameSortKey() }
        SortOption.FloorPriceChangeDesc,
        SortOption.MktCapFiatDesc -> tickers?.sortedByDescending { it.nftFloorSortKey() }
        SortOption.FloorPriceChangeAsc,
        SortOption.MktCapFiatAsc -> tickers?.sortedBy { it.nftFloorSortKey() }
        SortOption.VolumeFiatDesc -> tickers?.sortedByDescending { it.nftVolumeSortKey() }
        SortOption.VolumeFiatAsc -> tickers?.sortedBy { it.nftVolumeSortKey() }
        else -> tickers?.sortedByDescending { it.nftFloorSortKey() }
    }
    val items = sorted?.map { it.toDetailMarketsItem() }
    return DetailMarketsConfig.NFT to items
}

fun NFTTicker.toDetailMarketsItem(): DetailMarketsItem {
    val symbol = native_currency_symbol?.trim()?.uppercase() ?: ""
    val currency = if (symbol.isEmpty()) {
        CurrencySelectionModel(
            currency = "",
            code = "",
            symbol = "",
            suffix = true,
        )
    } else {
        val spelledSymbol = symbol.toCharArray().joinToString(" ") { it.toString() }
        CurrencySelectionModel(
            currency = spelledSymbol,
            code = symbol,
            symbol = symbol,
            suffix = true,
            readAccessibilityAsSymbol = true,
        )
    }
    val floorPrice = floor_price_in_native_currency

    return DetailMarketsItem(
        imageUrl = image,
        name = name,
        price = floorPrice?.formatCurrency(FormattingThreshold.NONE, currency = currency),
        volume = null,
        tradeUrl = nft_collection_url,
        priceAccessible = floorPrice?.formatCurrency(
            threshold = FormattingThreshold.NONE,
            currency = currency,
            accessible = true,
        ),
    )
}

fun ExchangeDetailViewModel.toDetailMarketsData(): Pair<DetailMarketsConfig, List<DetailMarketsItem>?> {
    val tickers = exchangeInfo?.tickers
    val sorted: List<ExchangeTicker>? = when (selectedSortOption) {
        SortOption.VolumeDesc -> tickers?.sortedByDescending { it.volumeSortKey() }
        SortOption.VolumeAsc -> tickers?.sortedBy { it.volumeSortKey() }
        SortOption.priceDesc -> tickers?.sortedByDescending { it.priceSortKey() }
        SortOption.priceAsc -> tickers?.sortedBy { it.priceSortKey() }
        SortOption.NameDesc -> tickers?.sortedBy { it.pairSortName() }
        SortOption.NameAsc -> tickers?.sortedByDescending { it.pairSortName() }
        else -> tickers?.sortedByDescending { it.volumeSortKey() }
    }
    val items = sorted?.map { it.toDetailMarketsItem(toplist) }
    return DetailMarketsConfig.Exchange to items
}

fun ExchangeTicker.toDetailMarketsItem(toplist: List<Toplist>?): DetailMarketsItem {
    val priceUsd = converted_last?.getSelectedCurrency()
    val volumeUsd = converted_volume?.getSelectedCurrency()
    val pairName = listOfNotNull(base, target).joinToString("/").ifEmpty { null }
    val imageUrl = toplist?.firstOrNull { it.symbol?.lowercase() == base?.lowercase() }?.image ?: market?.logo
    return DetailMarketsItem(
        imageUrl = imageUrl,
        name = pairName,
        price = priceUsd?.formatCurrency(FormattingThreshold.NONE),
        volume = volumeUsd?.formatCurrency(),
        tradeUrl = trade_url,
        priceAccessible = priceUsd?.formatCurrency(FormattingThreshold.NONE, accessible = true),
        volumeAccessible = volumeUsd?.formatCurrency(accessible = true),
    )
}
