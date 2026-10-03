package com.cryptium.coincurrently.core.networking
import com.cryptium.coincurrently.core.models.CarouselFilter
import kotlinx.serialization.Serializable

@Serializable
sealed class NewsCategory {
    abstract val type: String

    val selectedId: String
        get() = when (this) {
            is Coin -> id
            is Exchange -> id
            else -> ""
        }

    val toCarouselFilter: CarouselFilter
        get() = when (this) {
            is Videos -> CarouselFilter.General
            is General -> CarouselFilter.General
            is Neutral -> CarouselFilter.Neutral
            is Positive -> CarouselFilter.Positive
            is Negative -> CarouselFilter.Negative
            is NFT -> CarouselFilter.Nfts
            is Podcasts -> CarouselFilter.General
            is Technical_Analysis -> CarouselFilter.TechnicalAnalysis
            is Coin -> CarouselFilter.Coins(id)
            is Exchange -> CarouselFilter.Exchanges(id)
            is Exchanges -> CarouselFilter.Exchanges("")
        }

    @Serializable
    data class General(val id: String = "General") : NewsCategory() {
        override val type: String = "General"
    }

    @Serializable
    data class Technical_Analysis(val id: String = "Technical_Analysis") : NewsCategory() {
        override val type: String = "Technical Analysis"
    }

    @Serializable
    data class Positive(val id: String = "Positive") : NewsCategory() {
        override val type: String = "Positive"
    }

    @Serializable
    data class Neutral(val id: String = "Neutral") : NewsCategory() {
        override val type: String = "Neutral"
    }

    @Serializable
    data class Negative(val id: String = "Negative") : NewsCategory() {
        override val type: String = "Negative"
    }

    @Serializable
    data class Coin(val id: String) : NewsCategory() {
        override val type: String = id
    }

    @Serializable
    data class Exchange(val id: String) : NewsCategory() {
        override val type: String = id
    }

    @Serializable
    data class Exchanges(val id: String = "Exchanges") : NewsCategory() {
        override val type: String = "Exchange"
    }

    @Serializable
    data class NFT(val id: String = "NFT") : NewsCategory() {
        override val type: String = "NFT"
    }

    @Serializable
    data class Podcasts(val id: String = "Podcasts") : NewsCategory() {
        override val type: String = "Podcasts"
    }

    @Serializable
    data class Videos(val id: String = "Videos") : NewsCategory() {
        override val type: String = "Videos"
    }
}
