package com.cryptium.coincurrently.ui.main.Repositories

import android.util.Log
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import networking.ApiHelper
import networking.CacheUrls
import networking.MainRepository
import networking.ResponseModels.Derivative
import networking.RetrofitBuilder

class DerivativeDetailRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        private val derivativeType = object : TypeToken<Derivative>() {}.type

        suspend fun getDerivativeDetail(
            id: String?
        ): Derivative? {
            val id = id ?: return null
            val url = CacheUrls.derivativeDetail(id)

            @Suppress("UNCHECKED_CAST")
            (RetrofitBuilder.getCached(url, derivativeType)?.data as? Derivative)?.let { data ->
                return data.copy(id = id)
            }

            Log.w("Network call ", "Derivative detail")

            val derivative = mainRepository.getDerivativeDetail(id).unwrapOrNull()
            return derivative?.copy(id = id)
        }
    }
}
