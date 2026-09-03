package com.cryptium.coincurrently.features.Home.Carousel
import com.cryptium.coincurrently.core.models.CarouselFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeCarouselDefaultTest {

    @Test
    fun defaultHomeFilter_opensFavoritesWhenUserHasFavorites() {
        assertEquals(CarouselFilter.Favorites, CarouselFilter.defaultHomeFilter(hasFavorites = true))
    }

    @Test
    fun defaultHomeFilter_opensTopCoinsWhenUserHasNoFavorites() {
        assertEquals(CarouselFilter.TopCoins, CarouselFilter.defaultHomeFilter(hasFavorites = false))
    }
}
