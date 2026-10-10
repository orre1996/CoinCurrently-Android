package com.cryptium.coincurrently.testsupport

import android.os.LocaleList
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.sharedui.models.CachedData
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
        val locales = mockk<LocaleList>()
        every { locales.size() } returns 1
        every { locales[any()] } returns Locale.US
        mockkStatic(LocaleList::class)
        every { LocaleList.getDefault() } returns locales
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
        unmockkAll()
        RetrofitBuilder.clearResponseCache()
    }
}

fun <T> seedCache(url: String, data: T, date: Date = Date()) {
    RetrofitBuilder.updateCache(url, CachedData(data, date))
}
