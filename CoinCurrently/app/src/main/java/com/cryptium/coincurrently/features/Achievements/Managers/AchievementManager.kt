package com.cryptium.coincurrently.features.Achievements.Managers
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.os.Handler
import android.os.Looper
import com.cryptium.coincurrently.core.extensions.allTransactionsCount
import com.cryptium.coincurrently.core.extensions.getProfitPercent
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertsRepository
import com.cryptium.coincurrently.features.Market.Repositories.ToplistRepository
import com.cryptium.coincurrently.features.Achievements.AchievementCatalog
import com.cryptium.coincurrently.features.Achievements.Models.AchievementCounterKey
import com.cryptium.coincurrently.features.Achievements.Models.AchievementMetric
import com.cryptium.coincurrently.features.Achievements.Models.AchievementProgress
import com.cryptium.coincurrently.features.Achievements.Models.AchievementUnlockEvent
import com.cryptium.coincurrently.features.Achievements.Models.AchievementCounters
import com.cryptium.coincurrently.features.Achievements.Models.AchievementsSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import com.cryptium.coincurrently.core.networking.ResponseModels.SearchItem
import com.cryptium.coincurrently.core.networking.ResponseModels.sanitizeGsonNullLists

object AchievementManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val computeMutex = Mutex()
    private var isCheckingUnlocks = false
    private var pendingRecheck = false

    var cachedProgress: List<AchievementProgress> = emptyList()
        private set
    var cachedSummary = AchievementsSummary(0, 0, 0, 0)
        private set

    fun increment(key: AchievementCounterKey) {
        val counters = loadCounters()
        val previousValue = counters.value(key).toDouble()
        counters.increment(key)
        saveCounters(counters)

        scope.launch {
            ensureSnapshotInitializedIfNeeded()
            detectAndDeliverUnlocks(
                metric = key.metric,
                previousValue = previousValue,
                newValue = counters.value(key).toDouble(),
            )
        }
    }

    fun checkMetricUnlock(metric: AchievementMetric, previousValue: Double, newValue: Double) {
        scope.launch {
            ensureSnapshotInitializedIfNeeded()
            invalidateProgressCache()
            detectAndDeliverUnlocks(metric, previousValue, newValue)
        }
    }

    fun checkPortfolioTransactionUnlock(previousCount: Int) {
        scope.launch {
            val newCount = PortfolioRepository.portfolio?.data?.data?.allTransactionsCount ?: 0
            ensureSnapshotInitializedIfNeeded()
            invalidateProgressCache()
            detectAndDeliverUnlocks(
                metric = AchievementMetric.portfolioTransactions,
                previousValue = previousCount.toDouble(),
                newValue = newCount.toDouble(),
            )
        }
    }

    fun currentMaxPortfolioGainPercent(): Double = maxPortfolioGainPercent()

    fun checkMoonShotUnlock(previousMaxGain: Double) {
        val newMaxGain = maxPortfolioGainPercent()
        if (newMaxGain == previousMaxGain) return
        checkMetricUnlock(
            metric = AchievementMetric.maxPortfolioGainPercent,
            previousValue = previousMaxGain,
            newValue = newMaxGain,
        )
    }

    fun checkForNewUnlocks() {
        scope.launch {
            if (isCheckingUnlocks) {
                pendingRecheck = true
                return@launch
            }

            isCheckingUnlocks = true
            try {
                ensureSnapshotInitializedIfNeeded()
                invalidateProgressCache()

                val progress = computeAllProgress()
                var snapshot = loadUnlockSnapshot()
                val newUnlocks = mutableListOf<AchievementUnlockEvent>()

                for (item in progress) {
                    val previousIndex = snapshot[item.definition.id] ?: -1
                    val currentIndex = item.unlockedLevelIndex

                    if (currentIndex > previousIndex) {
                        for (index in (previousIndex + 1)..currentIndex) {
                            val level = item.definition.levels[index]
                            newUnlocks.add(
                                AchievementUnlockEvent(
                                    achievementTitle = item.definition.title,
                                    levelName = level.name,
                                )
                            )
                        }
                    }

                    snapshot[item.definition.id] = maxOf(snapshot[item.definition.id] ?: -1, currentIndex)
                }

                saveUnlockSnapshot(snapshot)
                deliverUnlocks(newUnlocks)
            } finally {
                isCheckingUnlocks = false
                if (pendingRecheck) {
                    pendingRecheck = false
                    checkForNewUnlocks()
                }
            }
        }
    }

    fun preloadProgress() {
        scope.launch {
            computeAllProgress()
        }
    }

    suspend fun computeAllProgress(): List<AchievementProgress> = computeMutex.withLock {
        withContext(Dispatchers.Default) {
            val metricValues = fetchMetricValues()
            val snapshot = loadUnlockSnapshot()
            val progress = AchievementCatalog.all.map { definition ->
                val currentValue = metricValues[definition.metric] ?: 0.0
                val computedIndex = definition.levels.indexOfLast { currentValue >= it.threshold }
                val snapshotIndex = snapshot[definition.id] ?: -1
                val unlockedIndex = maxOf(computedIndex, snapshotIndex)
                AchievementProgress(
                    definition = definition,
                    currentValue = currentValue,
                    unlockedLevelIndex = unlockedIndex,
                )
            }
            updateCachedProgress(progress)
            progress
        }
    }

    fun computeSummary(progressList: List<AchievementProgress>): AchievementsSummary {
        val unlockedLevels = progressList.sumOf { maxOf(0, it.unlockedLevelIndex + 1) }
        val totalLevels = AchievementCatalog.all.sumOf { it.levels.size }
        return AchievementsSummary(
            unlockedLevels = unlockedLevels,
            totalLevels = totalLevels,
            currentStreak = AchievementStreakTracker.currentStreak,
            longestStreak = AchievementStreakTracker.longestStreak,
        )
    }

    private suspend fun ensureSnapshotInitializedIfNeeded() {
        val isInitialized: Boolean = StorageManager.get(StorageKeys.ACHIEVEMENT_UNLOCK_SNAPSHOT_INITIALIZED) ?: false
        if (isInitialized) return

        val progress = computeAllProgress()
        val snapshot = progress.associate { it.definition.id to it.unlockedLevelIndex }
        saveUnlockSnapshot(snapshot)
        StorageManager.set(StorageKeys.ACHIEVEMENT_UNLOCK_SNAPSHOT_INITIALIZED, true)
    }

    private fun detectAndDeliverUnlocks(
        metric: AchievementMetric,
        previousValue: Double,
        newValue: Double,
    ) {
        val definitions = AchievementCatalog.all.filter { it.metric == metric }
        if (definitions.isEmpty()) return

        var snapshot = loadUnlockSnapshot()
        val newUnlocks = mutableListOf<AchievementUnlockEvent>()

        for (definition in definitions) {
            val previousIndex = definition.levels.indexOfLast { previousValue >= it.threshold }
            val currentIndex = definition.levels.indexOfLast { newValue >= it.threshold }
            val snapshotIndex = snapshot[definition.id] ?: -1

            val startIndex =
                if (currentIndex > previousIndex) {
                    previousIndex + 1
                } else {
                    maxOf(previousIndex, snapshotIndex) + 1
                }

            if (currentIndex >= startIndex) {
                for (index in startIndex..currentIndex) {
                    val level = definition.levels[index]
                    newUnlocks.add(
                        AchievementUnlockEvent(
                            achievementTitle = definition.title,
                            levelName = level.name,
                        )
                    )
                }
            }

            snapshot[definition.id] = maxOf(snapshot[definition.id] ?: -1, currentIndex)
        }

        saveUnlockSnapshot(snapshot)
        deliverUnlocks(newUnlocks)
    }

    private fun loadCounters(): AchievementCounters =
        StorageManager.get(StorageKeys.ACHIEVEMENT_COUNTERS) ?: AchievementCounters()

    private fun saveCounters(counters: AchievementCounters) {
        StorageManager.set(StorageKeys.ACHIEVEMENT_COUNTERS, counters)
    }

    private fun loadUnlockSnapshot(): MutableMap<String, Int> =
        StorageManager.get(StorageKeys.ACHIEVEMENT_UNLOCK_SNAPSHOT) ?: mutableMapOf()

    private fun saveUnlockSnapshot(snapshot: Map<String, Int>) {
        StorageManager.set(StorageKeys.ACHIEVEMENT_UNLOCK_SNAPSHOT, snapshot.toMutableMap())
    }

    private fun deliverUnlocks(unlocks: List<AchievementUnlockEvent>) {
        for (unlock in unlocks) {
            Handler(Looper.getMainLooper()).post {
                AchievementNavigationManager.showUnlock(unlock)
            }
        }
    }

    private fun invalidateProgressCache() {
        // Progress is recomputed on demand; cached values updated in computeAllProgress.
    }

    private fun updateCachedProgress(progress: List<AchievementProgress>) {
        cachedProgress = progress
        cachedSummary = computeSummary(progress)
    }

    private fun uniqueSearchCount(): Double {
        val history: Map<String, List<SearchItem>?> =
            StorageManager.get(StorageKeys.SEARCH_HISTORY) ?: emptyMap()
        return history.values.filterNotNull().flatten().mapNotNull { it.id }.toSet().size.toDouble()
    }

    private fun maxPortfolioGainPercent(): Double {
        val portfolio = PortfolioRepository.portfolio?.data?.data ?: return 0.0
        val prices = PortfolioRepository.portfolioAllCurrencies ?: return 0.0
        val showCurrency = UserPreferences.getSelectedCurrency().code.lowercase()

        var maxGain = 0.0
        for (pie in portfolio) {
            pie.sanitizeGsonNullLists()
            val coinId = pie.data?.id ?: continue
            val transactions = pie.transactions
            val coinPrices = prices[coinId]
            val gain = transactions.getProfitPercent(coinPrices, showCurrency) ?: continue
            if (gain.isFinite()) {
                maxGain = maxOf(maxGain, gain)
            }
        }
        return maxOf(0.0, maxGain)
    }

    private fun fetchMetricValues(): Map<AchievementMetric, Double> {
        val counters = loadCounters()
        val favoritesCount = ToplistRepository.favorites?.size?.toDouble() ?: 0.0
        val transactionCount = PortfolioRepository.portfolio?.data?.data?.allTransactionsCount?.toDouble() ?: 0.0
        val alertsCount = PriceAlertsRepository.priceAlerts?.size?.toDouble() ?: 0.0
        val widgetCount = CurrencyRepository.widgetCurrencies.size.toDouble()
        val authEnabled = UserPreferences.getIsBiometricAuthenticationEnabled()

        return mapOf(
            AchievementMetric.dailyStreak to AchievementStreakTracker.currentStreak.toDouble(),
            AchievementMetric.favoritesCount to favoritesCount,
            AchievementMetric.portfolioTransactions to transactionCount,
            AchievementMetric.maxPortfolioGainPercent to maxPortfolioGainPercent(),
            AchievementMetric.priceAlertsCount to alertsCount,
            AchievementMetric.widgetCoinsCount to widgetCount,
            AchievementMetric.uniqueSearches to uniqueSearchCount(),
            AchievementMetric.cryptoDetailsViewed to counters.cryptoDetailsViewed.toDouble(),
            AchievementMetric.compareSessions to counters.compareSessions.toDouble(),
            AchievementMetric.newsArticlesOpened to counters.newsArticlesOpened.toDouble(),
            AchievementMetric.nftDetailsViewed to counters.nftDetailsViewed.toDouble(),
            AchievementMetric.exchangeDetailsViewed to counters.exchangeDetailsViewed.toDouble(),
            AchievementMetric.sectorDetailsViewed to counters.sectorDetailsViewed.toDouble(),
            AchievementMetric.fearGreedVisits to counters.fearGreedVisits.toDouble(),
            AchievementMetric.gasFeesVisits to counters.gasFeesVisits.toDouble(),
            AchievementMetric.treasuryVisits to counters.treasuryVisits.toDouble(),
            AchievementMetric.candleChartToggles to counters.candleChartToggles.toDouble(),
            AchievementMetric.converterUses to counters.converterUses.toDouble(),
            AchievementMetric.portfolioExports to counters.portfolioExports.toDouble(),
            AchievementMetric.authenticationEnabled to if (authEnabled) 1.0 else 0.0,
            AchievementMetric.referralsCompleted to counters.referralsCompleted.toDouble(),
            AchievementMetric.selfRedeems to counters.selfRedeems.toDouble(),
        )
    }
}
