package com.karanchuk.ratesapp.repository

import com.karanchuk.ratesapp.api.RatesApi
import com.karanchuk.ratesapp.api.RevolutApi
import com.karanchuk.ratesapp.ui.main.RateUI
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface RevolutRepository {

    suspend fun requestRates(base: String): List<RateUI>
}

class RevolutRepositoryImpl @Inject constructor(
    private val revolutApi: RevolutApi
) : RevolutRepository {

    override suspend fun requestRates(base: String): List<RateUI> {
        val ratesApi = getRates(Dispatchers.IO, base).await()
        return ratesApi!!.rates.map {
            RateUI(it.value, it.key, "currency name")
        }
    }

    private fun getRates(
        coroutineContext: CoroutineContext,
        base: String
    ): Deferred<RatesApi?> {
        return CoroutineScope(coroutineContext).async {
            revolutApi.getRates(base).execute().body()
        }
    }
}