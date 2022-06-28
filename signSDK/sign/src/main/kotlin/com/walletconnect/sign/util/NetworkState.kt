@file:JvmSynthetic

package com.walletconnect.sign.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.walletconnect.sign.core.scope.scope
import kotlinx.coroutines.flow.*

private enum class Transport {
    UNKNOWN, CELLULAR, WIFI
}

internal class NetworkState(context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _isWiFiAvailable = MutableStateFlow(false)
    private val _isCellularAvailable = MutableStateFlow(false)
    private var currentTransport: Transport = Transport.UNKNOWN

    val isAvailable: StateFlow<Boolean> = combine(_isWiFiAvailable, _isCellularAvailable)
    { wifi, cellular -> wifi || cellular }.stateIn(scope, SharingStarted.Eagerly, false)

    private val callback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {

            connectivityManager.getNetworkCapabilities(network)?.let { capabilities ->

                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    _isWiFiAvailable.compareAndSet(expect = false, update = true)
                }

                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    _isCellularAvailable.compareAndSet(expect = false, update = true)
                }
            }
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)

            currentTransport = when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> Transport.WIFI
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> Transport.CELLULAR
                else -> Transport.UNKNOWN
            }
        }

        override fun onLost(network: Network) {
            if (currentTransport == Transport.WIFI) {
                _isWiFiAvailable.compareAndSet(expect = true, update = false)
            }

            if (currentTransport == Transport.CELLULAR) {
                _isCellularAvailable.compareAndSet(expect = true, update = false)
            }
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
    }
}

