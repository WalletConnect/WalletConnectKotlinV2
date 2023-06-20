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
import org.koin.core.KoinApplication


interface CoreInterface<T : CoreInterface.CoreDelegate> {
    val Pairing: PairingInterface
    val PairingController: PairingControllerInterface
    val Relay: RelayConnectionInterface
    val Echo: EchoInterface
    val Verify: VerifyInterface
    val Sync: SyncInterface
    var koinApp: KoinApplication

    interface CoreDelegate : PairingInterface.Delegate

    // Generic required not to break previous CoreClient.CoreDelegate invocations
    fun setDelegate(delegate: T)

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


/** Create an instrumented test to verify CoreClient works on one app?
 * - Same pairing on wallet and dapp
 * - Is metadata table all good -> two entries that only differ in peer type
 */