package com.karanchuk.ratesapp.common

import com.karanchuk.ratesapp.ui.main.RateUI

object Utils {

    fun roundRateAmounts(rates: List<RateUI>) {
        rates.forEach {
            val amountRounded = String.format("%.2f", it.amount.toFloat())
            val amountRoundedFloat = amountRounded.toFloat()
            val amountInt = amountRoundedFloat.toInt()
            val fract = amountRoundedFloat - amountInt
            it.amount = if (fract == 0f) {
                amountInt.toString()
            } else {
                amountRounded
            }
        }
    }
}


