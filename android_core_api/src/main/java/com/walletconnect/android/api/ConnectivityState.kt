package com.walletconnect.android.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectivityState(context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateIfStateChanged()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateIfStateChanged()
        }

        override fun onLost(network: Network) {
            updateIfStateChanged()
        }
    }

    private fun updateIfStateChanged() {
        val isNowConnected = isConnected()
        val wasConnected = _isAvailable.value

        if (!wasConnected && isNowConnected) {
            _isAvailable.compareAndSet(expect = false, update = true)
        } else if (wasConnected && !isNowConnected) {
            _isAvailable.compareAndSet(expect = true, update = false)
        }
    }

    private fun isConnected(): Boolean = connectivityManager.run {
        getNetworkCapabilities(activeNetwork)?.run {
            hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
    } ?: false


    init {
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), callback)
    }
}