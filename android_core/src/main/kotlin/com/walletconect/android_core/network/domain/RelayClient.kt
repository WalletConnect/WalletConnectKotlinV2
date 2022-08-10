@file:JvmSynthetic

package com.walletconect.android_core.network.domain

import com.walletconect.android_core.network.RelayConnectionInterface
import com.walletconect.android_core.network.data.connection.controller.ConnectionController
import com.walletconnect.foundation.network.BaseRelayClient
import com.walletconnect.foundation.network.data.service.RelayService
import com.walletconnect.foundation.util.Logger
import com.walletconect.android_core.common.WRONG_CONNECTION_TYPE
import kotlinx.coroutines.CoroutineScope

class RelayClient internal constructor(
    private val connectionController: ConnectionController,
    private val relay: RelayService,
    logger: Logger,
    scope: CoroutineScope,
) : BaseRelayClient(relay, logger, scope), RelayConnectionInterface {

    override fun connect(onError: (String) -> Unit) {
        when (connectionController) {
            is ConnectionController.Automatic -> onError(WRONG_CONNECTION_TYPE)
            is ConnectionController.Manual -> connectionController.connect()
        }
    }

    override fun disconnect(onError: (String) -> Unit) {
        when (connectionController) {
            is ConnectionController.Automatic -> onError(WRONG_CONNECTION_TYPE)
            is ConnectionController.Manual -> connectionController.disconnect()
        }
    }
}