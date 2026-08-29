package com.cryptium.coincurrently.ui.main.Repositories

import AddressImport
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import networking.ApiHelper
import networking.MainRepository
import networking.RetrofitBuilder

class WalletImportRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        suspend fun getWalletAddresses(chain: String, address: String): AddressImport? {
            return mainRepository.getWalletAddress(chain, address).unwrapOrNull()
        }
    }
}