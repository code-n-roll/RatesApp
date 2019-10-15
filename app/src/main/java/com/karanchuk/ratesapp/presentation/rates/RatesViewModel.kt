package com.karanchuk.ratesapp.presentation.rates

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.karanchuk.ratesapp.data.repository.RevolutRepositoryImpl
import com.karanchuk.ratesapp.domain.common.Utils
import com.karanchuk.ratesapp.domain.common.livedata.NetworkLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class RatesViewModel @Inject constructor(
    private val repository: RevolutRepositoryImpl,
    val networkLiveData: NetworkLiveData
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
    private val timer = Observable.interval(0, 1, TimeUnit.SECONDS)
    private var timerDisposable: Disposable? = null
    private val compositeDisposable = CompositeDisposable()
    private var currentBaseRate = RateUI("1", "USD", "United States Dollar")

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + viewModelJob

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        compositeDisposable.clear()
    }

    private fun loadRates() {
        launch {
            try {
                if (currentBaseRate.amount.toDoubleOrNull() == null) {
                    return@launch
                }
                val rates = repository.requestRates(currentBaseRate.currencyCode)
                val ratesLinkedList = LinkedList<RateUI>(rates)
                ratesLinkedList.addFirst(currentBaseRate)

                Utils.convertRatesBy(ratesLinkedList, currentBaseRate.amount.toDouble())
                Utils.roundRateAmounts(ratesLinkedList)

                _rates.postValue(ratesLinkedList)
            } catch (e: Throwable) {
                e.printStackTrace()
                _rates.postValue(null)
            }
        }
    }

    private fun subscribeToTimer() {
        if (timerDisposable == null || timerDisposable?.isDisposed!!) {
            timerDisposable = timer
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        Log.d(TAG, "Timer tick")
                        loadRates()
                    }
                )
        }
        compositeDisposable.add(timerDisposable!!)
    }

    private fun unsubscribeFromTimer() {
        compositeDisposable.remove(timerDisposable!!)
    }

    internal fun resumeTimer() {
        subscribeToTimer()
    }

    internal fun pauseTimer() {
        unsubscribeFromTimer()
    }

    internal fun updateCurrentBaseRate(rate: RateUI) {
        currentBaseRate = rate
    }
}
