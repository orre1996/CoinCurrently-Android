package com.cryptium.coincurrently.features.PriceAlerts
import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertCreateResult
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertsRepository
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.AlertCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.PostAlertCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

@SuppressLint("MutableCollectionMutableState")
class PriceAlertsViewModel: ViewModel() {

    private val priceAlertsRepository by lazy { PriceAlertsRepository }
    private val cryptoDetailRepository by lazy { CurrencyRepository }
    private val toplistRepository by lazy { ToplistRepository }

    var alerts: List<AlertCoin>? by mutableStateOf(null)
    var alertPrices: MutableMap<String, Map<String, Double>>? by mutableStateOf(null)

    var filteredToplist: MutableList<Toplist>? by mutableStateOf(null)

    var groups: List<String> by mutableStateOf(listOf())

    var isAlertsEnabled: Boolean by mutableStateOf(StorageManager.get<Boolean>(StorageKeys.SUBSCIBRES_TO_BIG_MOVERS) ?: true)

    var notificationsEnabled: Boolean by mutableStateOf(false)

    init {
        toplistRepository.smallToplist?.let {
            filteredToplist = it
        }

        priceAlertsRepository.alertPricesAllCurrencies?.let {
            alertPrices = it
        }

        priceAlertsRepository.priceAlerts?.let {
            alerts = it
            setGroups()
        }
    }

    suspend fun getFilteredToplist() {
        toplistRepository.getSmallToplist()?.let { toplist ->
            val toplist = toplist.sortedBy { it.market_cap_rank ?: 9999 }
            getAlertPrices(toplist.mapNotNull { it.id })
            filteredToplist = toplist.toMutableList()

            val toplistIdSet = filteredToplist?.mapNotNull { it.id }?.toSet()?.toList() ?: listOf()
            val alertCoinIdsNotInToplist = alerts?.map { it.coinId }?.subtract(toplistIdSet)?.toList() ?: listOf()

            if (alertCoinIdsNotInToplist.isNotEmpty()) {
                getRemainingItems(alertCoinIdsNotInToplist)
            }
        }
    }

    suspend fun getAlertPrices(ids: List<String>) {
        priceAlertsRepository.getAlertPrices(ids)?.let {
            alertPrices = it
        }
    }

    private suspend fun getRemainingItems(ids: List<String>) {
        val remainingCoins = priceAlertsRepository.downloadRemainingCoins(ids) ?: return
        val newList = filteredToplist?.plus(remainingCoins)?.toSet()?.toList() ?: remainingCoins
        filteredToplist = newList.sortedBy { it.market_cap_rank ?: 9999 }.toMutableList()
    }

    fun setGroups() {
        val groupsMap = HashMap<String, Int>()
        alerts?.forEach { alert ->
            val marketCap = filteredToplist?.firstOrNull { it.id == alert.coinId }?.market_cap_rank ?: 9999
            groupsMap[alert.coinId] = marketCap
        }

        groups = groupsMap.entries.sortedBy { it.value }.map { it.key }
    }

    fun getCoin(id: String?): Toplist? {
        if (id == null) {
            return null
        }

        return filteredToplist?.firstOrNull({ it.id == id })
    }

    suspend fun addPriceAlert(alertCoin: PostAlertCoin): PriceAlertCreateResult {
        return when (val result = priceAlertsRepository.createAlert(alertCoin)) {
            PriceAlertCreateResult.Success -> {
                alerts = priceAlertsRepository.priceAlerts
                setGroups()
                PriceAlertCreateResult.Success
            }
            else -> result
        }
    }

    suspend fun getPriceAlerts() {
        priceAlertsRepository.getAlerts()?.let {
            alerts = it
        } ?: run {
            alerts = listOf()
        }
    }

    suspend fun deleteAlert(alert: AlertCoin) {
        priceAlertsRepository.deleteAlert(alert.id)?.let {
            alerts = it
            setGroups()
        }
    }

    suspend fun getSelectedCoin(id: String): CryptoDetailInfoModel? {
        return cryptoDetailRepository.getCryptoDetail(id)
    }

    suspend fun toggleBigMover() {
        val isOn: Boolean

        if (isAlertsEnabled) {
            isOn = priceAlertsRepository.ubsubBigMover()
        } else {
            isOn = priceAlertsRepository.subBigMovers()
        }

        isAlertsEnabled = isOn
    }

    suspend fun sendFeedback(rating: Int, feedback: String?): Boolean {
        return priceAlertsRepository.sendFeedback(rating = rating, feedback = feedback)
    }
}