package com.cryptium.coincurrently.features.Portfolio
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.core.networking.MainRepository

object WalletImportRepository {
    suspend fun getWalletAddresses(chain: String, address: String): AddressImport? {
        return MainRepository.getWalletAddress(chain, address).unwrapOrNull()
    }
}
