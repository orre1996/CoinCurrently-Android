package com.cryptium.coincurrently.ui.main.HomeTab.Home.Enums

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
