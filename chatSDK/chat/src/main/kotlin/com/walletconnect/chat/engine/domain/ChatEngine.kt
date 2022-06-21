@file:JvmSynthetic

package com.walletconnect.chat.engine.domain

import com.walletconnect.chat.copiedFromSign.core.scope.scope
import com.walletconnect.chat.copiedFromSign.crypto.KeyManagementRepository
import com.walletconnect.chat.core.model.vo.AccountId
import com.walletconnect.chat.core.model.vo.AccountIdWithPublicKeyVO
import com.walletconnect.chat.core.model.vo.EventsVO
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterAccountUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveAccountUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class ChatEngine(
    private val registerAccountUseCase: RegisterAccountUseCase,
    private val resolveAccountUseCase: ResolveAccountUseCase,
    private val keyManagementRepository: KeyManagementRepository,
) {
    private val _events: MutableSharedFlow<EventsVO> = MutableSharedFlow()
    val events: SharedFlow<EventsVO> = _events.asSharedFlow()

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
}