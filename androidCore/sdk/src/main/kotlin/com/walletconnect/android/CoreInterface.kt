package com.walletconnect.android

import android.app.Application
import com.walletconnect.android.echo.EchoInterface
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.NetworkClientTimeout
import com.walletconnect.android.relay.RelayConnectionInterface
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.verify.VerifyInterface


interface CoreInterface {
    val Pairing: PairingInterface
    val PairingController: PairingControllerInterface
    val Relay: RelayConnectionInterface
    val Echo: EchoInterface
    val Verify: VerifyInterface
    val Sync: SyncInterface

    interface Delegate : PairingInterface.Delegate

    fun setDelegate(delegate: Delegate)

    fun initialize(
        metaData: Core.Model.AppMetaData,
        relayServerUrl: String,
        connectionType: ConnectionType,
        application: Application,
        relay: RelayConnectionInterface? = null,
        keyServerUrl: String? = null,
        networkClientTimeout: NetworkClientTimeout? = null,
        onError: (Core.Model.Error) -> Unit,
    )
}