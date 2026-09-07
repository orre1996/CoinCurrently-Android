package com.cryptium.coincurrently.features.Portfolio.ViewModels
import com.cryptium.coincurrently.core.networking.ResponseModels.Chain
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.TransactionAdded
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.sanitizeGsonNullLists
import java.util.UUID

class ImportWalletTransactionViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportWalletTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImportWalletTransactionViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ImportWalletTransactionViewModel(): ViewModel() {
    private val portfolioRepository by lazy { PortfolioRepository }
    private val currencyRepository by lazy { CurrencyRepository }
    private val toplistRepository by lazy { ToplistRepository }

    var walletName: MutableState<String> = mutableStateOf("")
    var address: MutableState<String> = mutableStateOf("")
    var selectedChain: MutableState<Chain> = mutableStateOf(Chain.BTC)
    var isImporting: Boolean by mutableStateOf(false)
    var showImportError: Boolean by mutableStateOf(false)

    val chains = Chain.entries.toTypedArray()

    var chainCoins: List<Toplist>? by mutableStateOf(null)

    suspend fun getToplist() {
        toplistRepository.getToplist(
            sortOption = SortOption.MktCapDesc,
            priceChange = PriceChange.SevenDays,
            platformId = null,
        )?.let {
            val chainIds = chains.map { it.coinId.lowercase() }
            chainCoins = it.filter { coin ->
                coin.id?.lowercase() in chainIds
            }
        }
    }

    suspend fun importWallet(context: Context): UUID? {
        showImportError = false
        isImporting = true

        val lookupAddress = address.value.trim()

        val coinData = currencyRepository.getCryptoDetail(id = selectedChain.value.coinId) ?: run {
            showImportError = true
            isImporting = false
            return null
        }

        val (portfolio, currencies) = portfolioRepository.importWallet(
            walletName = walletName.value.ifEmpty { null },
            coinData = coinData,
            address = address.value,
            chain = selectedChain.value
        )

        if (portfolio == null || currencies == null) {
            showImportError = true
            isImporting = false
            return null
        }

        AnalyticsManager.log(
            TransactionAdded(
                ScreenLabel.ImportWalletTransaction,
                coinData.name ?: "",
            ),
        )

        isImporting = false
        MainActivity.showAppReview(context)

        return portfolioRepository.portfolio?.data?.data
            ?.asSequence()
            ?.flatMap { it.sanitizeGsonNullLists().imports.asSequence() }
            ?.filter { it.address.equals(lookupAddress, ignoreCase = true) }
            ?.maxByOrNull { it.date.time }
            ?.id
    }
}