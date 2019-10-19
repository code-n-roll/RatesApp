package com.karanchuk.ratesapp.domain

import com.karanchuk.ratesapp.presentation.rates.RateUI

data class Rate(
    var amount: Double,
    var currencyCode: String
) {

    constructor(rateUI: RateUI): this(rateUI.amount.toDouble(), rateUI.currencyCode)

    override fun toString(): String {
        val amountRounded = String.format("%.2f", amount)
        val amountRoundedDouble = amountRounded.toFloat()
        val amountInt = amountRoundedDouble.toInt()
        val fract = amountRoundedDouble - amountInt
        return if (fract == 0f) {
            amountInt.toString()
        } else {
            amountRounded
        }
    }
}

data class Rates(
    private val _rates: List<Rate>,
    private var _baseRate: Rate
) {

    constructor(ratesUI: List<RateUI>, baseRateUI: RateUI) : this(ratesUI.map(::Rate), Rate(baseRateUI))

    internal val convertedRates: List<Rate>
        get() = getConvertedRates()

    private fun getConvertedRates(): List<Rate> {
        for ((i, rate) in _rates.withIndex()) {
            if (i != 0) {
                val rateAmountPerOne = rate.amount
                val count = _baseRate.amount
                _rates[i].amount = count * rateAmountPerOne
            }
        }
        return _rates
    }
}
