@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class NetworkState(context: Context) {

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { _isAvailable.compareAndSet(expect = false, update = true) }
        override fun onLost(network: Network) { _isAvailable.compareAndSet(expect = true, update = false) }
    }

    init {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).registerDefaultNetworkCallback(callback)
    }
}

