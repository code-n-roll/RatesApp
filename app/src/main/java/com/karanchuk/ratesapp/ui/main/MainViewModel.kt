package com.karanchuk.ratesapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val rates: MutableLiveData<List<RateUI>> by lazy {
        MutableLiveData<List<RateUI>>().also {
            loadRates()
        }
    }

    fun getRates(): LiveData<List<RateUI>> {
        return rates
    }

    private fun loadRates() {
        // TODO: do an async operation to fetch rates

    }
}
