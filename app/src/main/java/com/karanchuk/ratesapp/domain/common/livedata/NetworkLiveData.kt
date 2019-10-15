package com.karanchuk.ratesapp.domain.common.livedata

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.LiveData
import com.karanchuk.ratesapp.domain.common.isOnline

class NetworkLiveData(
    private val context: Context,
    private val connectivityManager: ConnectivityManager
) : LiveData<LiveEvent<Boolean>>() {

    private var broadcastReceiver: BroadcastReceiver? = null

    private fun prepareReceiver() {
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                value = LiveEvent(connectivityManager.isOnline())
            }
        }
        context.registerReceiver(broadcastReceiver, filter)
    }

    override fun onActive() {
        super.onActive()
        prepareReceiver()
    }

    override fun onInactive() {
        super.onInactive()
        context.unregisterReceiver(broadcastReceiver)
        broadcastReceiver = null
    }
}