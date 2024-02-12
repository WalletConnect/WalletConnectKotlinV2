package com.walletconnect.sample.dapp.ui

import com.walletconnect.android.Core

sealed class DappSampleEvents {

    object SessionApproved : DappSampleEvents()

    object SessionRejected : DappSampleEvents()
    object RequestExpired : DappSampleEvents()

    object SessionAuthenticateApproved : DappSampleEvents()
    object SessionAuthenticateRejected : DappSampleEvents()

    data class PingSuccess(val topic: String) : DappSampleEvents()

    object PingError : DappSampleEvents()

    object PingLoading : DappSampleEvents()

    object Disconnect : DappSampleEvents()
    data class DisconnectError(val message: String) : DappSampleEvents()

    object DisconnectLoading : DappSampleEvents()

    data class RequestSuccess(val result: String) : DappSampleEvents()

    data class RequestPeerError(val errorMsg: String) : DappSampleEvents()

    data class RequestError(val exceptionMsg: String) : DappSampleEvents()

    object NoAction : DappSampleEvents()

    data class SessionEvent(val name: String, val data: String) : DappSampleEvents()

    object SessionExtend : DappSampleEvents()

    data class ConnectionEvent(val isAvailable: Boolean) : DappSampleEvents()

    data class PairingExpired(val pairing: Core.Model.Pairing) : DappSampleEvents()

    object ProposalExpired : DappSampleEvents()
}