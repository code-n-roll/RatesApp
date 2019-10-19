package com.karanchuk.ratesapp.domain.common

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt


object Utils {

    fun dpToPx(context: Context, dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
}

class DelayException : Exception()


