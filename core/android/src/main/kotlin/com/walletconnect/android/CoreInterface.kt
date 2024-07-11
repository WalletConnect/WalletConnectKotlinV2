package com.walletconnect.android

import android.app.Application
import com.walletconnect.android.internal.common.explorer.ExplorerInterface
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.push.PushInterface
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.NetworkClientTimeout
import com.walletconnect.android.relay.RelayConnectionInterface
import com.walletconnect.android.verify.client.VerifyInterface

interface CoreInterface {
    val Pairing: PairingInterface
    val PairingController: PairingControllerInterface
    val Relay: RelayConnectionInterface
    @Deprecated(message = "Replaced with Push")
    val Echo: PushInterface
    val Push: PushInterface
    val Verify: VerifyInterface
    val Explorer: ExplorerInterface

    interface Delegate : PairingInterface.Delegate

    fun setDelegate(delegate: Delegate)

    fun initialize(
        metaData: Core.Model.AppMetaData,
        relayServerUrl: String,
        connectionType: ConnectionType = ConnectionType.AUTOMATIC,
        application: Application,
        relay: RelayConnectionInterface? = null,
        keyServerUrl: String? = null,
        networkClientTimeout: NetworkClientTimeout? = null,
        telemetryEnabled: Boolean = true,
        onError: (Core.Model.Error) -> Unit,
    )

    fun initialize(
        application: Application,
        projectId: String,
        metaData: Core.Model.AppMetaData,
        connectionType: ConnectionType = ConnectionType.AUTOMATIC,
        relay: RelayConnectionInterface? = null,
        keyServerUrl: String? = null,
        networkClientTimeout: NetworkClientTimeout? = null,
        telemetryEnabled: Boolean = true,
        onError: (Core.Model.Error) -> Unit,
    )
}