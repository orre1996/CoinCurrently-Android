package com.cryptium.coincurrently.features.DerivativeDetail
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative
import com.cryptium.coincurrently.core.networking.RetrofitBuilder

object DerivativeDetailRepository {
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

        val derivative = MainRepository.getDerivativeDetail(id).unwrapOrNull()
        return derivative?.copy(id = id)
    }
}
