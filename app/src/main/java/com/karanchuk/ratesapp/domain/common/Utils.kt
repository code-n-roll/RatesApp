package com.karanchuk.ratesapp.domain.common

import android.content.Context
import com.karanchuk.ratesapp.presentation.rates.RateUI
import android.util.DisplayMetrics
import kotlin.math.roundToInt


object Utils {

    fun roundRateAmounts(rates: List<RateUI>) {
        rates.drop(1).forEach { rateUI ->
            val amountRounded = String.format("%.2f", rateUI.amount.toFloat())
            val amountRoundedFloat = amountRounded.toFloat()
            val amountInt = amountRoundedFloat.toInt()
            val fract = amountRoundedFloat - amountInt
            rateUI.amount = if (fract == 0f) {
                amountInt.toString()
            } else {
                amountRounded
            }
        }
    }

    fun convertRatesBy(rates: List<RateUI>, baseRateAmount: Double) {
        for ((i, rate) in rates.withIndex()) {
            if (i != 0) {
                val rateAmountPerOne = rate.amount.toDouble()
                val count = baseRateAmount
                rates[i].amount = (count * rateAmountPerOne).toString()
            }
        }
    }

    fun dpToPx(context: Context, dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
}


