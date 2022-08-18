@file:JvmSynthetic

package com.walletconnect.auth.engine.domain

import com.walletconnect.android_core.common.model.SDKError
import com.walletconnect.android_core.common.WalletConnectException
import com.walletconnect.android_core.common.model.ConnectionState
import com.walletconnect.android_core.common.model.type.EngineEvent
import com.walletconnect.android_core.common.scope.scope
import com.walletconnect.android_core.crypto.KeyManagementRepository
import com.walletconnect.auth.engine.model.EngineDO
import com.walletconnect.auth.json_rpc.domain.JsonRpcInteractor
import com.walletconnect.auth.storage.AuthStorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class AuthEngine(
    private val relayer: JsonRpcInteractor,
    private val crypto: KeyManagementRepository,
    private val storage: AuthStorageRepository,
    private val metaData: EngineDO.AppMetaData,
) {

    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    init {
        resubscribeToSequences()
        collectInternalErrors()
    }

    fun handleInitializationErrors(onError: (WalletConnectException) -> Unit) {
        relayer.initializationErrorsFlow.onEach { walletConnectException -> onError(walletConnectException) }.launchIn(scope)
    }

    private fun resubscribeToSequences() {
        relayer.isConnectionAvailable
            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) { resubscribeToPairings() }
                }
            }
            .launchIn(scope)
    }

    private fun resubscribeToPairings() {
//        val (listOfExpiredPairing, listOfValidPairing) =
//            sequenceStorageRepository.getListOfPairingVOs().partition { pairing -> !pairing.expiry.isSequenceValid() }
//
//        listOfExpiredPairing
//            .map { pairing -> pairing.topic }
//            .onEach { pairingTopic ->
//                relayer.unsubscribe(pairingTopic)
//                crypto.removeKeys(pairingTopic.value)
//                sequenceStorageRepository.deletePairing(pairingTopic)
//            }
//
//        listOfValidPairing
//            .map { pairing -> pairing.topic }
//            .onEach { pairingTopic -> relayer.subscribe(pairingTopic) }
    }

    private fun collectInternalErrors() {
        relayer.internalErrors
            .onEach { exception -> _engineEvent.emit(SDKError(exception)) }
            .launchIn(scope)
    }


}