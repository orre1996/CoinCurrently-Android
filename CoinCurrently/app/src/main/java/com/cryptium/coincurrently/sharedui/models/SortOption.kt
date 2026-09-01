package com.cryptium.coincurrently.sharedui.models
/**
 * Unified sort option for toplist, NFTs, categories, and exchanges.
 */
enum class SortOption {
    // Shared
    MktCapDesc,
    MktCapAsc,
    NameDesc,
    NameAsc,
    // Toplist
    PriceChangeDesc,
    PriceChangeAsc,
    VolumeDesc,
    VolumeAsc,
    // Exchanges
    TrustScoreDesc,
    TrustScoreAsc,
    // Categories
    MktCapChangeDesc,
    MktCapChangeAsc,
    // NFT
    FloorPriceDesc,
    FloorPriceAsc,
    FloorPriceChangeDesc,
    FloorPriceChangeAsc,
    MktCapFiatDesc,
    MktCapFiatAsc,
    VolumeFiatDesc,
    VolumeFiatAsc,

    openInterestDesc,

    openInterestAsc,

    priceDesc,

    priceAsc;

    // MARK: - Context-specific option lists


    companion object {
        val toplistOptions: Array<SortOption>
            get() = arrayOf(
                MktCapDesc,
                MktCapAsc,
                PriceChangeDesc,
                PriceChangeAsc,
                VolumeDesc,
                VolumeAsc
            )

        val sectorOptions: Array<SortOption>
            get() = arrayOf(
                MktCapDesc,
                MktCapAsc,
                MktCapChangeDesc,
                MktCapChangeAsc,
                VolumeDesc,
                VolumeAsc
            )

        val nftOptions: Array<SortOption>
            get() = arrayOf(
                FloorPriceDesc,
                FloorPriceAsc,
                FloorPriceChangeDesc,
                FloorPriceChangeAsc,
                MktCapFiatDesc,
                MktCapFiatAsc,
                VolumeFiatDesc,
                VolumeFiatAsc
            )

        val categoriesOptions: Array<SortOption>
            get() = arrayOf(
                MktCapDesc,
                MktCapAsc,
                MktCapChangeDesc,
                MktCapChangeAsc,
                NameDesc,
                NameAsc
            )

        val exchangesOptions: Array<SortOption>
            get() = arrayOf(
                TrustScoreDesc,
                TrustScoreAsc,
                VolumeDesc,
                VolumeAsc,
                NameDesc,
                NameAsc
            )

        val derivativesOptions: Array<SortOption>
            get() = arrayOf(
                openInterestDesc,
                openInterestAsc,
                VolumeDesc,
                VolumeAsc,
                NameDesc,
                NameAsc
            )

        val derivativeDetailOptions: Array<SortOption>
            get() = arrayOf(
                openInterestDesc,
                openInterestAsc,
                priceDesc,
                priceAsc
            )

        val nftDetailOptions: Array<SortOption>
            get() = arrayOf(
                FloorPriceDesc,
                FloorPriceAsc,
                NameDesc,
                NameAsc
            )

        val exchangeDetailOptions: Array<SortOption>
            get() = arrayOf(
                VolumeDesc,
                VolumeAsc,
                priceDesc,
                priceAsc,
                NameDesc,
                NameAsc
            )
    }

    // MARK: - API values (context-dependent)

    /** API value for toplist (coins) and exchanges endpoints */
    val toplistApiValue: String
        get() = when (this) {
            MktCapDesc -> "market_cap_desc"
            MktCapAsc -> "market_cap_asc"
            PriceChangeDesc -> "price_change_desc"
            PriceChangeAsc -> "price_change_asc"
            VolumeDesc -> "volume_desc"
            VolumeAsc -> "volume_asc"
            TrustScoreDesc -> "trust_score_desc"
            TrustScoreAsc -> "trust_score_asc"
            NameDesc -> "name_desc"
            NameAsc -> "name_asc"
            else -> "market_cap_desc"
        }

    /** API value for categories endpoint */
    val categoriesApiValue: String
        get() = when (this) {
            MktCapDesc -> "market_cap_desc"
            MktCapAsc -> "market_cap_asc"
            MktCapChangeDesc -> "market_cap_change_24h_desc"
            MktCapChangeAsc -> "market_cap_change_24h_asc"
            NameDesc -> "name_asc"
            NameAsc -> "name_desc"
            else -> "market_cap_desc"
        }

    /** API value for derivatives exchanges endpoint */
    val derivativesApiValue: String
        get() = when (this) {
            openInterestDesc -> "open_interest_btc_desc"
            openInterestAsc -> "open_interest_btc_asc"
            VolumeDesc -> "trade_volume_24h_btc_desc"
            VolumeAsc -> "trade_volume_24h_btc_asc"
            NameDesc -> "name_asc"
            NameAsc -> "name_desc"
            else -> "open_interest_btc_desc"
        }

    /** API value for NFT endpoint (floor price & mkt cap share API value; client-side sort differs) */
    val nftApiValue: String
        get() = when (this) {
            FloorPriceDesc, FloorPriceChangeDesc, MktCapFiatDesc -> "market_cap_usd_desc"
            FloorPriceAsc, FloorPriceChangeAsc, MktCapFiatAsc -> "market_cap_usd_asc"
            VolumeFiatDesc -> "h24_volume_usd_desc"
            VolumeFiatAsc -> "h24_volume_usd_asc"
            else -> "market_cap_usd_desc"
        }

    // MARK: - Toplist cache key (includes price change for price change sorts)

    fun getOptionKey(priceChange: PriceChange): String =
        when (this) {
            MktCapDesc, MktCapAsc, VolumeDesc, VolumeAsc, TrustScoreDesc, TrustScoreAsc ->
                toplistApiValue
            PriceChangeDesc, PriceChangeAsc ->
                "$toplistApiValue-${priceChange.rawValue}"
            else ->
                toplistApiValue
        }

    // MARK: - Display

    val title: String
        get() = when (this) {
            MktCapDesc -> "Market Cap ↓"
            MktCapAsc -> "Market Cap ↑"
            PriceChangeDesc -> "Price Change ↓"
            PriceChangeAsc -> "Price Change ↑"
            VolumeDesc -> "Volume ↓"
            VolumeAsc -> "Volume ↑"
            NameDesc -> "Name (A → Z)"
            NameAsc -> "Name (Z → A)"
            MktCapChangeDesc -> "Market Cap Change ↓"
            MktCapChangeAsc -> "Market Cap Change ↑"
            FloorPriceDesc -> "Floor Price ↓"
            FloorPriceAsc -> "Floor Price ↑"
            FloorPriceChangeDesc -> "Floor Price Change ↓"
            FloorPriceChangeAsc -> "Floor Price Change ↑"
            MktCapFiatDesc -> "Market Cap ↓"
            MktCapFiatAsc -> "Market Cap ↑"
            VolumeFiatDesc -> "Volume Fiat ↓"
            VolumeFiatAsc -> "Volume Fiat ↑"
            TrustScoreDesc -> "Trust Score ↓"
            TrustScoreAsc -> "Trust Score ↑"
            openInterestDesc -> "Open Interest ↓"
            openInterestAsc -> "Open Interest ↑"
            priceDesc -> "Price ↓"
            priceAsc -> "Price ↑"
        }

    val accessibilityTitle: String
        get() = when (this) {
            MktCapDesc -> "Market Cap descending"
            MktCapAsc -> "Market Cap ascending"
            PriceChangeDesc -> "Price Change descending"
            PriceChangeAsc -> "Price Change ascending"
            VolumeDesc -> "Volume descending"
            VolumeAsc -> "Volume ascending"
            NameDesc -> "Name ascending"
            NameAsc -> "Name descending"
            MktCapChangeDesc -> "Market Cap Change descending"
            MktCapChangeAsc -> "Market Cap Change ascending"
            FloorPriceDesc -> "Floor Price descending"
            FloorPriceAsc -> "Floor Price ascending"
            FloorPriceChangeDesc -> "Floor Price Change descending"
            FloorPriceChangeAsc -> "Floor Price Change ascending"
            MktCapFiatDesc -> "Market Cap descending"
            MktCapFiatAsc -> "Market Cap ascending"
            VolumeFiatDesc -> "Volume Fiat descending"
            VolumeFiatAsc -> "Volume Fiat ascending"
            TrustScoreDesc -> "Trust Score descending"
            TrustScoreAsc -> "Trust Score ascending"
            openInterestDesc -> "Open Interest descending"
            openInterestAsc -> "Open Interest ascending"
            priceDesc -> "Price descending"
            priceAsc -> "Price ascending"
        }

    val subtitle: String
        get() = when (this) {
            MktCapDesc -> "Sort by market cap from highest to lowest"
            MktCapAsc -> "Sort by market cap from lowest to highest"
            PriceChangeDesc -> "Sort by % price change from highest to lowest"
            PriceChangeAsc -> "Sort by % price change from lowest to highest"
            VolumeDesc -> "Sort by volume from highest to lowest"
            VolumeAsc -> "Sort by volume from lowest to highest"
            NameDesc -> "Sort by name from a to z"
            NameAsc -> "Sort by name from z to a"
            MktCapChangeDesc -> "Sort by market cap change from highest to lowest"
            MktCapChangeAsc -> "Sort by market cap change from lowest to highest"
            FloorPriceDesc -> "Sort by floor price from highest to lowest"
            FloorPriceAsc -> "Sort by floor price from lowest to highest"
            FloorPriceChangeDesc -> "Sort by floor price change from highest to lowest"
            FloorPriceChangeAsc -> "Sort by floor price change from lowest to highest"
            MktCapFiatDesc -> "Sort by market cap from highest to lowest"
            MktCapFiatAsc -> "Sort by market cap from lowest to highest"
            VolumeFiatDesc -> "Sort by volume fiat from highest to lowest"
            VolumeFiatAsc -> "Sort by volume fiat from lowest to highest"
            TrustScoreDesc -> "Sort by trust score from highest to lowest"
            TrustScoreAsc -> "Sort by trust score from lowest to highest"
            openInterestDesc -> "Sort by open interest from highest to lowest"
            openInterestAsc -> "Sort by open interest from lowest to highest"
            priceDesc -> "Sort by price from highest to lowest"
            priceAsc -> "Sort by price from lowest to highest"
        }
}
