package com.karanchuk.ratesapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.karanchuk.ratesapp.common.Utils
import com.karanchuk.ratesapp.repository.RevolutRepositoryImpl
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

class MainViewModel @Inject constructor(
    private val repository: RevolutRepositoryImpl
) : ViewModel(), CoroutineScope {

    // Job should be initialized before others its dependencies
    private val viewModelJob = Job()
    private val _rates: MutableLiveData<List<RateUI>> by lazy {
        MutableLiveData<List<RateUI>>().also {
            loadRates()
        }
    }
    internal val rates: LiveData<List<RateUI>> = _rates
    private val timer = Observable.interval(1, 1, TimeUnit.SECONDS)
    private val compositeDisposable = CompositeDisposable()
    private var currentBaseRate = RateUI("1", "USD", "US Dollar")

    private lateinit var timerDisposable: Disposable

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + viewModelJob

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        compositeDisposable.clear()
    }

    private fun loadRates() {
        // TODO: do an async operation to fetch rates
        launch {
            try {
                val rates = repository.requestRates(currentBaseRate.currencyCode)
                val ratesLinkedList = LinkedList<RateUI>(rates)
                ratesLinkedList.addFirst(currentBaseRate)

                for ((i, rate) in ratesLinkedList.withIndex()) {
                    if (i != 0) {
                        val rateAmountPerOne = rate.amount.toDouble()
                        val count = currentBaseRate.amount.toDouble()
                        ratesLinkedList[i].amount = (count * rateAmountPerOne).toString()
                    }
                }

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
