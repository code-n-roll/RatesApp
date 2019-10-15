package com.karanchuk.ratesapp.domain.common.livedata

class LiveEvent<out T>(private val content: T) {

    var isHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (isHandled) {
            null
        } else {
            isHandled = true
            content
        }
    }

    fun peekContent(): T = content
}