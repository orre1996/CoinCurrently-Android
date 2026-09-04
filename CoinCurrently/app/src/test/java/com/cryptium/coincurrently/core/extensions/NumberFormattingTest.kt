package com.cryptium.coincurrently.core.extensions
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberFormattingTest {

    private val usd = CurrencySelectionModel()

    @Test
    fun roundTo_halfUpAtTwoPlaces() {
        assertEquals(1.24, 1.235.roundTo(2), 0.0)
        assertEquals(1.0, 1.0.roundTo(2), 0.0)
    }

    @Test
    fun roundOrZero_collapsesTinyValues() {
        assertEquals(0.0, 0.0004.roundOrZero(3), 0.0)
        assertEquals(0.001, 0.0006.roundOrZero(3), 0.0)
    }

    @Test
    fun getDifferenceInPercent_isRelativeToTheBase() {
        assertEquals(50.0, getDifferenceInPercent(between = 150.0, and = 100.0)!!, 0.0001)
        assertEquals(0.0, getDifferenceInPercent(between = 0.0, and = 0.0)!!, 0.0)
    }

    @Test
    fun getChangeInValue_appliesPercentToCurrent() {
        assertEquals(10.0, 10.0.getChangeInValue(currentValue = 110.0), 0.0001)
    }

    @Test
    fun formatChangePercent_usesCompactSuffixAboveOneThousand() {
        assertEquals("12.34%", 12.34.formatChangePercent())
        assertEquals("150.0%", 150.0.formatChangePercent())
        assertEquals("1.23K%", 1234.0.formatChangePercent())
        assertEquals("∞%", Double.POSITIVE_INFINITY.formatChangePercent())
        assertEquals("-∞%", Double.NEGATIVE_INFINITY.formatChangePercent())
    }

    @Test
    fun formattedRemovingTrailingZeros_dropsNeedlessDecimals() {
        assertEquals("1", 1.0.formattedRemovingTrailingZeros())
        assertEquals("1.5", 1.50.formattedRemovingTrailingZeros())
    }

    @Test
    fun formatCurrency_usesUsdSymbolAndCompactThreshold() {
        assertTrue(100.0.formatCurrency(currency = usd).contains("100"))
        assertTrue(100.0.formatCurrency(currency = usd).contains("$"))
        assertTrue(
            1_500_000.0.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = usd,
            ).contains("M"),
        )
    }
}
