package com.karanchuk.ratesapp.data.repository

import com.karanchuk.ratesapp.data.api.RatesApi
import com.karanchuk.ratesapp.data.api.RevolutApi
import com.karanchuk.ratesapp.domain.Rate
import com.karanchuk.ratesapp.domain.common.DelayException
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface RevolutRepository {

    suspend fun requestRates(base: String): List<Rate>
}

class RevolutRepositoryImpl @Inject constructor(
    private val revolutApi: RevolutApi
) : RevolutRepository {

    override suspend fun requestRates(base: String): List<Rate> {
        val ratesApi = getRates(Dispatchers.IO, base).await()
        return ratesApi!!.rates.map {
            Rate(it.value.toDouble(), it.key)
        }
    }

    private fun getRates(
        coroutineContext: CoroutineContext,
        base: String
    ): Deferred<RatesApi?> {
        return CoroutineScope(coroutineContext).async {
            val startTime = System.currentTimeMillis()

            revolutApi.getRates(base).execute().body().also {
                val endTime = System.currentTimeMillis()
                if (endTime - startTime > 1_000) {
                    throw DelayException()
                }
            }
        }
    }
}