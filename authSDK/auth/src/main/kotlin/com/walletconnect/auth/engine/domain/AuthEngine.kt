package com.walletconnect.auth.engine.domain

import com.walletconnect.android_core.common.WalletConnectException
import com.walletconnect.android_core.common.scope.scope
import com.walletconnect.android_core.crypto.KeyManagementRepository
import com.walletconnect.auth.engine.model.EngineDO
import com.walletconnect.auth.json_rpc.domain.JsonRpcInteractor
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class AuthEngine(
    private val relayer: JsonRpcInteractor,
    private val crypto: KeyManagementRepository,
    private val metaData: EngineDO.AppMetaData,
) {

    fun handleInitializationErrors(onError: (WalletConnectException) -> Unit) {
        relayer.initializationErrorsFlow.onEach { walletConnectException -> onError(walletConnectException) }.launchIn(scope)
    }
}