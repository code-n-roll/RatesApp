package com.karanchuk.ratesapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.karanchuk.ratesapp.repository.RevolutRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    val rates: LiveData<List<RateUI>> = _rates

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + viewModelJob

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private fun loadRates() {
        // TODO: do an async operation to fetch rates
        launch {
            try {
                _rates.postValue(repository.requestRates("USD"))
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}
