package com.walletconnect.sample.web3inbox.ui.routes

sealed class W3ISampleEvents {

    data class SessionApproved(val account: String) : W3ISampleEvents()

    object SessionRejected : W3ISampleEvents()

    data class PingSuccess(val topic: String) : W3ISampleEvents()

    object PingError : W3ISampleEvents()

    object Disconnect : W3ISampleEvents()

    data class RequestSuccess(val result: String) : W3ISampleEvents()

    data class RequestPeerError(val errorMsg: String) : W3ISampleEvents()

    data class RequestError(val exceptionMsg: String) : W3ISampleEvents()

    object NoAction : W3ISampleEvents()

    data class SessionEvent(val name: String, val data: String): W3ISampleEvents()

    object SessionExtend: W3ISampleEvents()
    data class ConnectionEvent(val isAvailable: Boolean): W3ISampleEvents()
}