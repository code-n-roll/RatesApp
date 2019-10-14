package com.karanchuk.ratesapp.presentation.rates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.karanchuk.ratesapp.domain.common.Utils
import com.karanchuk.ratesapp.data.repository.RevolutRepositoryImpl
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
    private val repository: RevolutRepositoryImpl
) : ViewModel(), CoroutineScope {

    private val viewModelJob = Job()
    private val _rates: MutableLiveData<List<RateUI>> by lazy {
        MutableLiveData<List<RateUI>>().also {
            loadRates()
        }
    }
    internal val rates: LiveData<List<RateUI>> = _rates
    private val timer = Observable.interval(0, 1, TimeUnit.SECONDS)
    private lateinit var timerDisposable: Disposable
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
                val rates = repository.requestRates(currentBaseRate.currencyCode)
                val ratesLinkedList = LinkedList<RateUI>(rates)
                ratesLinkedList.addFirst(currentBaseRate)

                Utils.convertRatesBy(ratesLinkedList, currentBaseRate.amount.toDouble())
                Utils.roundRateAmounts(ratesLinkedList)

                _rates.postValue(ratesLinkedList)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun subscribeToTimer() {
        timerDisposable = timer
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    loadRates()
                }
            )
        compositeDisposable.add(timerDisposable)
    }

    private fun unsubscribeFromTimer() {
        compositeDisposable.remove(timerDisposable)
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
