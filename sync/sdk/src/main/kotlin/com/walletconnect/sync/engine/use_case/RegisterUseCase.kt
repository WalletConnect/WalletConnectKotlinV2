package com.walletconnect.sync.engine.use_case

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.signature.Signature
import com.walletconnect.sync.common.exception.validateAccountId
import com.walletconnect.sync.common.model.Account
import com.walletconnect.sync.common.model.toEntropy
import com.walletconnect.sync.storage.AccountsStorageRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class RegisterUseCase(private val accountsRepository: AccountsStorageRepository) : RegisterUseCaseInterface, GetMessageUseCaseInterface by GetMessageUseCase {

    override fun register(accountId: AccountId, signature: Signature, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                validateAccountId(accountId) { error -> return@supervisorScope onFailure(error) }

                val message = getMessage(accountId)
                //todo verify signature matches account and message
                // It will be a bigger refactor so created task for future https://github.com/WalletConnect/WalletConnectKotlinV2/issues/774
                runCatching { accountsRepository.createAccount(Account(accountId, message.toEntropy())) }.fold(
                    onSuccess = { onSuccess() },
                    onFailure = { error -> onFailure(error) }
                )
            }
        }
    }
}

internal interface RegisterUseCaseInterface {
    fun register(accountId: AccountId, signature: Signature, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}

