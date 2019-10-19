package com.karanchuk.ratesapp.presentation.rates

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.karanchuk.ratesapp.data.Currencies
import com.karanchuk.ratesapp.data.repository.RevolutRepositoryImpl
import com.karanchuk.ratesapp.domain.Rate
import com.karanchuk.ratesapp.domain.Rates
import com.karanchuk.ratesapp.domain.common.DelayException
import com.karanchuk.ratesapp.domain.common.livedata.NetworkLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class RatesViewModel @Inject constructor(
    private val repository: RevolutRepositoryImpl,
    val networkLiveData: NetworkLiveData,
    private var currencies: Currencies
) : ViewModel(), CoroutineScope {

    companion object {
        private const val TAG = "RatesViewModel"
    }

    private val viewModelJob = Job()
    private val _rates: MutableLiveData<List<RateUI>> by lazy {
        MutableLiveData<List<RateUI>>()
    }
    internal val rates: LiveData<List<RateUI>>
        get() = _rates
    private val _viewEffect: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    internal val viewEffect: LiveData<Boolean>
        get() = _viewEffect
    private val tickerChannel = ticker(1_000, 0).also {
        launch {
            for (event in it) {
                if (isTickerResumed) {
                    Log.d(TAG, "Timer tick")
                    loadRates()
                }
            }
        }
    }
    private var currentBaseRate = RateUI("1", "USD", "", "")
    private var isTickerResumed = false

    override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + viewModelJob

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        tickerChannel.cancel()
    }

    private fun loadRates() {
        if (currentBaseRate.amount.toDoubleOrNull() == null) {
            return
        }
        launch {
            try {
                val rates = repository.requestRates(currentBaseRate.currencyCode)

                val ratesLinkedList = LinkedList<Rate>(rates)
                ratesLinkedList.addFirst(Rate(currentBaseRate))
                val ratesDomain = Rates(ratesLinkedList, Rate(currentBaseRate))

                _rates.postValue(RatesUI(ratesDomain.convertedRates).roundedRatesUI.also {
                    it.forEach { rateUI ->
                        rateUI.currencyName = currencies.codeToName[rateUI.currencyCode] ?: ""
                        rateUI.flagFileName = currencies.codeToFlagImage[rateUI.currencyCode] ?: ""
                    }
                })
            } catch (e: Throwable) {
                when(e) {
                    is HttpException -> {
                        e.printStackTrace()
                        _rates.postValue(null)
                    }
                    is DelayException -> {
                        _viewEffect.postValue(true)
                    }
                }
            }
        }
    }

    internal fun resumeTimer() {
        isTickerResumed = true
    }

    internal fun pauseTimer() {
        isTickerResumed = false
    }

    internal fun updateCurrentBaseRate(rate: RateUI) {
        currentBaseRate = rate
    }
}
