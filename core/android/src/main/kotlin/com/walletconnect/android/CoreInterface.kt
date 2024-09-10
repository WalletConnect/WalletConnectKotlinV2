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

@Deprecated("com.walletconnect.android.CoreInterface has been deprecated. Please use com.reown.android.CoreInterface instead from - https://github.com/reown-com/reown-kotlin")
interface CoreInterface {
    val Pairing: PairingInterface
    val PairingController: PairingControllerInterface
    val Relay: RelayConnectionInterface
    @Deprecated(message = "Replaced with Push")
    val Echo: PushInterface
    val Push: PushInterface
    val Verify: VerifyInterface
    val Explorer: ExplorerInterface

    @Deprecated("com.walletconnect.android.CoreInterface has been deprecated. Please use com.reown.android.CoreInterface instead from - https://github.com/reown-com/reown-kotlin")
    interface Delegate : PairingInterface.Delegate

    @Deprecated("com.walletconnect.android.CoreInterface has been deprecated. Please use com.reown.android.CoreInterface instead from - https://github.com/reown-com/reown-kotlin")
    fun setDelegate(delegate: Delegate)

    @Deprecated("com.walletconnect.android.CoreClient has been deprecated. Please use com.reown.android.CoreClient instead from - https://github.com/reown-com/reown-kotlin")
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

    @Deprecated("com.walletconnect.android.CoreClient has been deprecated. Please use com.reown.android.CoreClient instead from - https://github.com/reown-com/reown-kotlin")
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