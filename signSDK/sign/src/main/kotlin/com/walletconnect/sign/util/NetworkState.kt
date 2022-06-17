package com.walletconnect.sign.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.walletconnect.sign.core.scope.scope
import kotlinx.coroutines.flow.*

internal class NetworkState(context: Context) {

    private val _isWiFiAvailable = MutableStateFlow(false)
    private val _isCellularAvailable = MutableStateFlow(false)

    //    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = combine(_isWiFiAvailable, _isCellularAvailable) { wifi, cellular ->
        if (!wifi && !cellular) false
        else wifi || cellular
    }.stateIn(scope, SharingStarted.Eagerly, false)

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {

            connectivityManager.getNetworkCapabilities(network)?.let { capabilities ->

                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Logger.error("kobe; onWifiAvailable; ${_isWiFiAvailable.value}")
                    _isWiFiAvailable.compareAndSet(expect = false, update = true)
                }

                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Logger.error("kobe; onCellularAvailable; ${_isCellularAvailable.value}")
                    _isCellularAvailable.compareAndSet(expect = false, update = true)
                }
            }
//            _isAvailable.compareAndSet(expect = false, update = true)
        }

        override fun onLost(network: Network) {

            connectivityManager.getNetworkCapabilities(network)?.let { capabilities ->

                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Logger.error("kobe; onWifiLost; ${_isWiFiAvailable.value}")
                    _isWiFiAvailable.compareAndSet(expect = true, update = false)
                }

                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Logger.error("kobe; onCellularLost; ${_isCellularAvailable.value}")
                    _isCellularAvailable.compareAndSet(expect = true, update = false)
                }
            }
//            _isAvailable.compareAndSet(expect = true, update = false)
        }
    }

    init {

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

//        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).registerDefaultNetworkCallback(callback)

        connectivityManager.registerNetworkCallback(request, callback)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//
//        } else {
//
//
//        }
    }
}

