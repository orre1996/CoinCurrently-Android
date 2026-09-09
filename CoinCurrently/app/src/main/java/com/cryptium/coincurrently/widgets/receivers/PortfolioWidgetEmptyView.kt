package com.cryptium.coincurrently.widgets.receivers

import android.content.Context
import android.widget.RemoteViews
import com.cryptium.coincurrently.R

internal fun createPortfolioWidgetEmptyRemoteViews(context: Context): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.portfolio_widget_empty)
    bindPortfolioWidgetEmptyRemoteViews(views, context)
    setSmallPortfolioClickIntent(views, context)
    return views
}

internal fun bindPortfolioWidgetEmptyRemoteViews(
    views: RemoteViews,
    context: Context,
) {
    val isDark = isWidgetDarkMode(context)
    views.setInt(
        R.id.ll_view,
        "setBackgroundResource",
        if (isDark) R.drawable.widget_background_dark else R.drawable.widget_background_light,
    )
    views.setInt(
        R.id.btn_add_transaction_container,
        "setBackgroundResource",
        if (isDark) {
            R.drawable.widget_portfolio_empty_button_background_dark
        } else {
            R.drawable.widget_portfolio_empty_button_background_light
        },
    )
    views.setTextColor(R.id.txt_add_transaction, widgetOnPrimary(context))
}
