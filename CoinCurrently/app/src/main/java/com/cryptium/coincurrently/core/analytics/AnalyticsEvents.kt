package com.cryptium.coincurrently.core.analytics
import android.os.Bundle
import com.cryptium.coincurrently.core.models.AppColorScheme
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.core.models.SelectedStartTab
import com.cryptium.coincurrently.core.models.CarouselFilter
import com.cryptium.coincurrently.core.models.ChartTypes

/**
 * Port of iOS analytics events; each type maps to an Aptabase custom event plus parameters.
 */
interface AnalyticsEvent {
    val eventName: String
    fun toBundle(): Bundle
}

internal fun bundleParams(vararg pairs: Pair<String, Any?>): Bundle {
    val b = Bundle()
    for ((k, v) in pairs) {
        when (v) {
            null -> Unit
            is String -> b.putString(k, v)
            is Int -> b.putLong(k, v.toLong())
            is Long -> b.putLong(k, v)
            is Double -> b.putDouble(k, v)
            is Boolean -> b.putLong(k, if (v) 1L else 0L)
            else -> b.putString(k, v.toString())
        }
    }
    return b
}

data class TabSwitched(
    val fromTab: ScreenLabel,
    val toTab: ScreenLabel,
) : AnalyticsEvent {
    override val eventName: String get() = "tab_switched"
    override fun toBundle(): Bundle = bundleParams(
        "from_tab" to fromTab.wire,
        "to_tab" to toTab.wire,
    )
}

data class TabChanged(
    val fromScreen: ScreenLabel,
    val selectedTabName: String,
) : AnalyticsEvent {
    override val eventName: String get() = "tab_changed"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "selected_tab" to selectedTabName,
    )
}

data class TimeframeChanged(
    val fromScreen: ScreenLabel,
    val days: String,
) : AnalyticsEvent {
    override val eventName: String get() = "timeframe_changed"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "days" to days,
    )
}

/**
 * GA4 custom event `navigate`. Firebase parameters use snake_case: **`from_screen`** and **`to_screen`**
 * ([ScreenLabel.wire], same labels as iOS `ScreenLabel.rawValue`).
 */
data class Navigate(
    val fromScreen: ScreenLabel,
    val toScreen: ScreenLabel,
    val coinName: String? = null,
    val nftName: String? = null,
    val sectorName: String? = null,
    val exchangeName: String? = null,
    val derivativeName: String? = null,
    val treasuryName: String? = null,
) : AnalyticsEvent {
    override val eventName: String get() = "navigate"
    override fun toBundle(): Bundle =
        Bundle().apply {
            putString("from_screen", fromScreen.wire)
            putString("to_screen", toScreen.wire)
            coinName?.let { putString("coin_name", it) }
            nftName?.let { putString("nft_name", it) }
            sectorName?.let { putString("sector_name", it) }
            exchangeName?.let { putString("exchange_name", it) }
            derivativeName?.let { putString("derivative_name", it) }
            treasuryName?.let { putString("treasury_name", it) }
        }
}

data class SearchHistoryItemAdded(
    val fromScreen: ScreenLabel,
    val itemName: String?,
    val query: String? = null,
) : AnalyticsEvent {
    override val eventName: String get() = "search_history_item_added"
    override fun toBundle(): Bundle {
        val base = bundleParams(
            "from_screen" to fromScreen.wire,
            "item_name" to (itemName ?: ""),
        )
        if (!query.isNullOrEmpty()) {
            base.putString("query", query)
        }
        return base
    }
}

data class SearchPerformed(
    val fromScreen: ScreenLabel,
    val query: String,
) : AnalyticsEvent {
    override val eventName: String get() = "search"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "search_term" to query,
    )
}

data class SheetOpened(
    val fromScreen: ScreenLabel,
    val sheet: SheetLabel,
) : AnalyticsEvent {
    override val eventName: String get() = "sheet_opened"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "sheet" to sheet.wire,
    )
}

data class ChangeCompareCoin(
    val fromScreen: ScreenLabel,
    val selection: CompareCoinChangeSelection,
    val coinName: String,
) : AnalyticsEvent {
    override val eventName: String get() = "change_compare_coin"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "selection" to selection.wire,
        "coin_name" to coinName,
    )
}

data class ChartTypeChanged(
    val fromScreen: ScreenLabel,
    val chartType: ChartTypes,
) : AnalyticsEvent {
    override val eventName: String get() = "chart_type_changed"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "chart_type" to chartType.firebaseAnalyticsChartTypeLabel(),
    )
}

data class ChartCandleModeChanged(
    val fromScreen: ScreenLabel,
    val isCandleChart: Boolean,
) : AnalyticsEvent {
    override val eventName: String get() = "chart_candle_mode_changed"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "mode" to if (isCandleChart) "candlestick" else "line",
    )
}

data class ChartFiatNativeModeChanged(
    val fromScreen: ScreenLabel,
    val isChartFiat: Boolean,
) : AnalyticsEvent {
    override val eventName: String get() = "chart_fiat_native_mode_changed"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "mode" to if (isChartFiat) "fiat" else "native",
    )
}

data class WebViewOpened(
    val fromScreen: ScreenLabel,
    val url: String,
) : AnalyticsEvent {
    override val eventName: String get() = "web_view_opened"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "url" to url,
    )
}

data class ScrollToTopPressed(
    val fromScreen: ScreenLabel,
    val selectedTabName: String? = null,
) : AnalyticsEvent {
    override val eventName: String get() = "scroll_to_top"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "selected_tab" to selectedTabName,
    )
}

data class ViewMoreExpanded(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "view_more_expanded"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class ViewMoreCollapsed(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "view_more_collapsed"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class PortfolioDataHide(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "portfolio_data_hide"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class PortfolioDataShow(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "portfolio_data_show"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class ClearFilters(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "clear_filters"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class TransactionTypeFilterAdded(
    val fromScreen: ScreenLabel,
    val transactionType: String,
) : AnalyticsEvent {
    override val eventName: String get() = "transaction_type_filter_added"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "transaction_type" to transactionType,
    )
}

data class TransactionTypeFilterRemoved(
    val fromScreen: ScreenLabel,
    val transactionType: String,
) : AnalyticsEvent {
    override val eventName: String get() = "transaction_type_filter_removed"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "transaction_type" to transactionType,
    )
}

data class TransactionTypeFilterCleared(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "transaction_type_filter_cleared"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class CoinFilterAdded(
    val fromScreen: ScreenLabel,
    val coinName: String,
) : AnalyticsEvent {
    override val eventName: String get() = "coin_filter_added"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "coin_name" to coinName,
    )
}

data class CoinFilterRemoved(
    val fromScreen: ScreenLabel,
    val coinName: String,
) : AnalyticsEvent {
    override val eventName: String get() = "coin_filter_removed"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "coin_name" to coinName,
    )
}

data class CoinFilterCleared(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "coin_filter_cleared"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class WalletAddressCopied(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "wallet_address_copied"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class WalletDeleted(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "wallet_deleted"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class PortfolioTransactionDeleted(
    val fromScreen: ScreenLabel,
    val coinName: String,
    val kind: PortfolioTransactionDeletedKind,
) : AnalyticsEvent {
    override val eventName: String get() = "portfolio_transaction_deleted"
    override fun toBundle(): Bundle {
        val b = bundleParams(
            "from_screen" to fromScreen.wire,
            "kind" to kind.wire,
        )
        if (coinName.isNotEmpty()) {
            b.putString("coin_name", coinName)
        }
        return b
    }
}

data class WalletRenamed(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "wallet_renamed"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class ResetApp(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "reset_app"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class BuyProOpened(
    val fromScreen: ScreenLabel,
    val action: BuyProAction,
) : AnalyticsEvent {
    override val eventName: String get() = "buy_pro_opened"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "action" to action.wire,
    )
}

data class ViewAll(
    val fromScreen: ScreenLabel,
    val filter: CarouselFilter,
) : AnalyticsEvent {
    override val eventName: String get() = "view_all"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "filter" to filter.firebaseAnalyticsFilterLabel(),
    )
}

data class PriceAlertAdded(
    val fromScreen: ScreenLabel,
    val coinName: String,
    val isRecurring: Boolean,
    val inputType: PriceAlertInputType,
) : AnalyticsEvent {
    override val eventName: String get() = "price_alert_added"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "coin_name" to coinName,
        "is_recurring" to isRecurring,
        "input_type" to inputType.wire,
    )
}

data class TransactionAdded(
    val fromScreen: ScreenLabel,
    val coinName: String,
) : AnalyticsEvent {
    override val eventName: String get() = "transaction_added"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "coin_name" to coinName,
    )
}

data class TransactionEdited(
    val fromScreen: ScreenLabel,
    val coinName: String,
) : AnalyticsEvent {
    override val eventName: String get() = "transaction_edited"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "coin_name" to coinName,
    )
}

data class DateFilterOptionSelected(
    val fromScreen: ScreenLabel,
    val option: String,
) : AnalyticsEvent {
    override val eventName: String get() = "date_filter_option_selected"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "option" to option,
    )
}

data class DateFilterCustomFieldOpened(
    val fromScreen: ScreenLabel,
    val field: DateFilterCustomField,
) : AnalyticsEvent {
    override val eventName: String get() = "date_filter_custom_field_opened"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "field" to field.wire,
    )
}

data class DateFilterCustomCleared(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "date_filter_custom_cleared"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class DateFilterCustomApplied(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "date_filter_custom_applied"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class DatePickerValueConfirmed(
    val fromScreen: ScreenLabel,
    val pickerKind: String,
) : AnalyticsEvent {
    override val eventName: String get() = "date_picker_value_confirmed"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "picker_kind" to pickerKind,
    )
}

data class PriceAlertDeleted(
    val fromScreen: ScreenLabel,
    val coinName: String,
) : AnalyticsEvent {
    override val eventName: String get() = "price_alert_deleted"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "coin_name" to coinName,
    )
}

data class HomeFilterSelected(
    val fromScreen: ScreenLabel,
    val filter: CarouselFilter,
) : AnalyticsEvent {
    override val eventName: String get() = "home_filter_selected"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "filter" to filter.firebaseAnalyticsFilterLabel(),
    )
}

data class SortOptionSelected(
    val fromScreen: ScreenLabel,
    val option: SortOption,
) : AnalyticsEvent {
    override val eventName: String get() = "sort_option_selected"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "option" to option.firebaseAnalyticsRawValue(),
    )
}

data class TabBarDoubleTapped(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "tab_bar_double_tapped"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class PriceChangeOptionSelected(
    val fromScreen: ScreenLabel,
    val option: PriceChange,
) : AnalyticsEvent {
    override val eventName: String get() = "price_change_option_selected"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "option" to option.firebaseAnalyticsOption(),
    )
}

data class NewsFilterSelected(
    val fromScreen: ScreenLabel,
    val filter: CarouselFilter,
) : AnalyticsEvent {
    override val eventName: String get() = "news_filter_selected"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "filter" to filter.firebaseAnalyticsFilterLabel(),
    )
}

data class ThemeSelected(
    val fromScreen: ScreenLabel,
    val theme: AppColorScheme,
) : AnalyticsEvent {
    override val eventName: String get() = "theme_selected"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "theme" to theme.firebaseAnalyticsThemeLabel(),
    )
}

data class StartTabSelected(
    val fromScreen: ScreenLabel,
    val tab: SelectedStartTab,
) : AnalyticsEvent {
    override val eventName: String get() = "start_tab_selected"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "tab" to tab.firebaseAnalyticsTabLabel(),
    )
}

data class CurrencySelected(
    val fromScreen: ScreenLabel,
    val currency: String,
) : AnalyticsEvent {
    override val eventName: String get() = "currency_selected"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "currency" to currency,
    )
}

data class SubscriptionOptionSelected(
    val fromScreen: ScreenLabel,
    val subscription: String,
) : AnalyticsEvent {
    override val eventName: String get() = "subscription_option_selected"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "subscription" to subscription,
    )
}

data class BuyProPressed(
    val fromScreen: ScreenLabel,
    val subscription: String,
) : AnalyticsEvent {
    override val eventName: String get() = "buy_pro_pressed"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "subscription" to subscription,
    )
}

/** iOS parity: logs after Play Billing resolves a purchase intent (StoreKit [`SubscriptionPurchaseCompleted`]). */
data class SubscriptionPurchaseCompleted(
    val fromScreen: ScreenLabel,
    val productId: String,
    val outcome: SubscriptionPurchaseOutcome,
) : AnalyticsEvent {
    override val eventName: String get() = "subscription_purchase_completed"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "product_id" to productId,
        "outcome" to outcome.wire,
    )
}

/** iOS parity: logs after restore finishes (StoreKit [`SubscriptionRestoreCompleted`]). */
data class SubscriptionRestoreCompleted(
    val fromScreen: ScreenLabel,
    val outcome: SubscriptionRestoreOutcome,
) : AnalyticsEvent {
    override val eventName: String get() = "subscription_restore_completed"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "outcome" to outcome.wire,
    )
}

data class NewsOpened(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "news_opened"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

/** iOS sends this as `news_opened`; kept identical for parity. */
data class NewsTabSelected(
    val fromScreen: ScreenLabel,
    val tab: String,
) : AnalyticsEvent {
    override val eventName: String get() = "news_opened"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "tab" to tab,
    )
}

data class FavoriteAdded(
    val coinName: String,
    val fromScreen: ScreenLabel,
    val carouselFilter: CarouselFilter? = null,
) : AnalyticsEvent {
    override val eventName: String get() = "favorite_added"
    override fun toBundle(): Bundle = bundleParams(
        "coin_name" to coinName,
        "from_screen" to fromScreen.wire,
        "carousel_filter" to carouselFilter?.firebaseAnalyticsFilterLabel(),
    )
}

data class FavoriteRemoved(
    val coinName: String,
    val fromScreen: ScreenLabel,
    val carouselFilter: CarouselFilter? = null,
) : AnalyticsEvent {
    override val eventName: String get() = "favorite_removed"
    override fun toBundle(): Bundle =
        bundleParams(
            "coin_name" to coinName,
            "from_screen" to fromScreen.wire,
            "carousel_filter" to carouselFilter?.firebaseAnalyticsFilterLabel(),
        )
}

data class AppShareActivityCompleted(
    val fromScreen: ScreenLabel,
    /** Android equivalent of UIActivity.ActivityType.rawValue, or `"none"`. */
    val activityType: String,
    val completed: Boolean,
) : AnalyticsEvent {
    override val eventName: String get() = "app_share_activity"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "share_activity_type" to activityType,
        "completed" to completed,
    )
}

data class EnabledFaceId(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "enabled_face_id"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class DisabledFaceId(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "disabled_face_id"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class EnabledColorBlindnessFilter(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "enabled_color_blindness_filter"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class DisabledColorBlindnessFilter(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "disabled_color_blindness_filter"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class EnabledWebSocket(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "enabled_web_socket"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class DisabledWebSocket(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "disabled_web_socket"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class EnabledBigMoveAlerts(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "enabled_big_move_alerts"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class DisabledBigMoveAlerts(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "disabled_big_move_alerts"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class OpenNotificationSettingsPressed(val fromScreen: ScreenLabel) : AnalyticsEvent {
    override val eventName: String get() = "open_notification_settings_pressed"
    override fun toBundle(): Bundle = bundleParams("from_screen" to fromScreen.wire)
}

data class FeedbackSent(
    val fromScreen: ScreenLabel,
    val rating: Int,
) : AnalyticsEvent {
    override val eventName: String get() = "feedback_sent"
    override fun toBundle(): Bundle = bundleParams(
        "from_screen" to fromScreen.wire,
        "rating" to rating,
    )
}
