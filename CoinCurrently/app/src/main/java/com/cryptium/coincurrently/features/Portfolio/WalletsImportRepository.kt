package com.cryptium.coincurrently.features.Portfolio
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.core.networking.ApiHelper
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.RetrofitBuilder

class WalletImportRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        suspend fun getWalletAddresses(chain: String, address: String): AddressImport? {
            return mainRepository.getWalletAddress(chain, address).unwrapOrNull()
        }
    }
}