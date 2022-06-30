@file:JvmSynthetic

package com.walletconnect.chat.engine.domain

import com.walletconnect.chat.copiedFromSign.core.scope.scope
import com.walletconnect.chat.copiedFromSign.crypto.KeyManagementRepository
import com.walletconnect.chat.copiedFromSign.json_rpc.domain.RelayerInteractor
import com.walletconnect.chat.copiedFromSign.util.Logger
import com.walletconnect.chat.core.model.vo.AccountId
import com.walletconnect.chat.core.model.vo.AccountIdWithPublicKeyVO
import com.walletconnect.chat.core.model.vo.EventsVO
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterAccountUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveAccountUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class ChatEngine(
    private val registerAccountUseCase: RegisterAccountUseCase,
    private val resolveAccountUseCase: ResolveAccountUseCase,
    private val keyManagementRepository: KeyManagementRepository,
    private val relayer: RelayerInteractor,
) {
    private val _events: MutableSharedFlow<EventsVO> = MutableSharedFlow()
    val events: SharedFlow<EventsVO> = _events.asSharedFlow()

    init {
        collectJsonRpcRequests()
        collectPeerResponses()
        relayer.initializationErrorsFlow.onEach { error -> Logger.error(error) }.launchIn(scope)
    }

    internal fun resolveAccount(accountId: AccountId, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                resolveAccountUseCase(accountId).fold(
                    onSuccess = { accountIdWithPublicKeyVO -> onSuccess(accountIdWithPublicKeyVO.publicKey.keyAsHex) },
                    onFailure = { error -> onFailure(error) }
                )
            }
        }
    }

    internal fun registerAccount(
        accountId: AccountId,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
        private: Boolean,
    ) {
        val (publicKey, _) = keyManagementRepository.getOrGenerateInviteSelfKeyPair()

        if (!private) {
            scope.launch {
                supervisorScope {
                    registerAccountUseCase(AccountIdWithPublicKeyVO(accountId, publicKey)).fold(
                        onSuccess = { onSuccess(publicKey.keyAsHex) },
                        onFailure = { error -> onFailure(error) }
                    )
                }
            }
        } else {
            onSuccess(publicKey.keyAsHex)
        }
    }

    private fun collectPeerResponses() {
        scope.launch {
            relayer.peerResponse.collect {

            }
        }
    }

    private fun collectJsonRpcRequests() {
        scope.launch {
            relayer.clientSyncJsonRpc.collect {

            }
        }
    }
}