package com.walletconnect.dapp.ui

sealed class SampleDappEvents {

    object SessionApproved : SampleDappEvents()

    object SessionRejected : SampleDappEvents()

    data class PingSuccess(val topic: String) : SampleDappEvents()

    object PingError : SampleDappEvents()

    object Disconnect : SampleDappEvents()

    data class RequestSuccess(val result: String) : SampleDappEvents()

    data class RequestPeerError(val errorMsg: String) : SampleDappEvents()

    data class RequestError(val exceptionMsg: String) : SampleDappEvents()

    object NoAction : SampleDappEvents()

    data class SessionEvent(val name: String, val data: String): SampleDappEvents()
}