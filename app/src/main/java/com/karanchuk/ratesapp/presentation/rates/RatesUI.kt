package com.karanchuk.ratesapp.presentation.rates

import com.karanchuk.ratesapp.domain.Rate

data class RateUI(
    var amount: String,
    var currencyCode: String,
    var currencyName: String,
    var flagFileName: String
) {
    constructor(rate: Rate) : this(rate.toString(), rate.currencyCode, "", "")
}

data class RatesUI(
    val roundedRatesUI: List<RateUI>
) {

    companion object {
        operator fun invoke(rates: List<Rate>) = RatesUI(rates.map(::RateUI))
    }
}