package com.cryptium.coincurrently.sharedui.components
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cryptium.coincurrently.app.NavigationViewModel

/** Re-runs [onCurrencyChanged] after the user selects a new display currency. */
@Composable
fun ObserveCurrencyChanges(
    navigationViewModel: NavigationViewModel,
    onCurrencyChanged: suspend () -> Unit,
) {
    val currencyRevision by navigationViewModel.currencyRevisionFlow.collectAsStateWithLifecycle()
    LaunchedEffect(currencyRevision) {
        if (currencyRevision == 0) return@LaunchedEffect
        onCurrencyChanged()
    }
}
