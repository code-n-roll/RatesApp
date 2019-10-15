package com.karanchuk.ratesapp.domain.common

import android.net.ConnectivityManager
import android.view.View

fun ConnectivityManager.isOnline(): Boolean {
    val netInfo = this.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
}

fun ConnectivityManager.isOffline(): Boolean {
    return !isOnline()
}

fun View.visible() {
    if (this.visibility != View.VISIBLE) {
        this.visibility = View.VISIBLE
    }
}

fun View.gone() {
    if (this.visibility != View.GONE) {
        this.visibility = View.GONE
    }
}

fun View.invisible() {
    if (this.visibility != View.INVISIBLE) {
        this.visibility = View.INVISIBLE
    }
}