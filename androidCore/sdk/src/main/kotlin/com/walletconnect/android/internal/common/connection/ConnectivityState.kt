@file:JvmSynthetic

package com.walletconnect.android.internal.common.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class ConnectivityState(context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()
    private val networks: MutableSet<Network> = mutableSetOf()

    private val callback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            if (network.isCapable()) {
                networks.add(network)
                _isAvailable.compareAndSet(expect = false, update = true)
            } else {
                _isAvailable.compareAndSet(expect = true, update = false)
            }
        }

        override fun onLost(network: Network) {
            networks.remove(network)

            if (networks.isNotEmpty()) {
                _isAvailable.compareAndSet(expect = false, update = true)
            } else {
                _isAvailable.compareAndSet(expect = true, update = false)
            }
        }
    }

    private fun Network.isCapable(): Boolean {
        return connectivityManager.getNetworkCapabilities(this)?.run {
            hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                    (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
        } ?: false
    }

    init {
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build(),
            callback
        )
    }
}