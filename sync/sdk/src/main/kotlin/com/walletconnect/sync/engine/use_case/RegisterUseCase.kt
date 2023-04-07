package com.walletconnect.sync.engine.use_case

import com.walletconnect.android.internal.common.cacao.signature.Signature
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.sync.common.model.Account
import com.walletconnect.sync.common.model.toEntropy
import com.walletconnect.sync.storage.AccountsStorageRepository
import kotlinx.coroutines.launch

internal class RegisterUseCase(private val accountsRepository: AccountsStorageRepository) : SuspendUseCase(), RegisterUseCaseInterface, GetMessageUseCaseInterface by GetMessageUseCase {

    override fun register(accountId: AccountId, signature: Signature, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        val message = getMessage(accountId) // Will be useful later. Wanted to see how other usecases could be put in
        //todo verify signature matches account and message
        // It will be a bigger refactor so created task for future https://github.com/WalletConnect/WalletConnectKotlinV2/issues/774
        scope.launch {
            runCatching { accountsRepository.createAccount(Account(accountId, message.toEntropy())) }.fold(
                onSuccess = { onSuccess() },
                onFailure = { error -> onFailure(error) }
            )
        }
    }
}

internal interface RegisterUseCaseInterface {
    fun register(accountId: AccountId, signature: Signature, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}

