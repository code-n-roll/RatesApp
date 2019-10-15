package com.karanchuk.ratesapp.domain.common

import android.net.ConnectivityManager

fun ConnectivityManager.isOnline(): Boolean {
    val netInfo = this.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
}

fun ConnectivityManager.isOffline(): Boolean {
    return !isOnline()
}