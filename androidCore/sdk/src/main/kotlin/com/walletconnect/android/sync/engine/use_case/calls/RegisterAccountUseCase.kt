package com.walletconnect.android.sync.engine.use_case.calls

import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.message.MessageSignatureVerifier
import com.walletconnect.android.sync.common.exception.InvalidSignatureException
import com.walletconnect.android.sync.common.exception.validateAccountId
import com.walletconnect.android.sync.common.model.Account
import com.walletconnect.android.sync.common.model.toEntropy
import com.walletconnect.android.sync.storage.AccountsStorageRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class RegisterAccountUseCase(private val accountsRepository: AccountsStorageRepository, private val messageSignatureVerifier: MessageSignatureVerifier) :
    RegisterUseCaseInterface, GetMessageUseCaseInterface by GetMessageUseCase {

    override fun register(accountId: AccountId, signature: String, signatureType: SignatureType, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                validateAccountId(accountId) { error -> return@supervisorScope onFailure(error) }

                // When account exists then return call onSuccess() and finish use case invocation
                if (!accountsRepository.doesAccountNotExists(accountId)) return@supervisorScope onSuccess()

                val message = getMessage(accountId)

                runCatching { messageSignatureVerifier.verify(signature, message, accountId.address(), signatureType) }
                    .onFailure { return@supervisorScope onFailure(InvalidSignatureException()) }
                    .onSuccess { isValid -> if (!isValid) return@supervisorScope onFailure(InvalidSignatureException()) }

                runCatching { accountsRepository.createAccount(Account(accountId, signature.toEntropy())) }.fold(
                    onSuccess = { onSuccess() },
                    onFailure = { error -> onFailure(error) }
                )
            }
        }
    }
}

internal interface RegisterUseCaseInterface {
    fun register(accountId: AccountId, signature: String, signatureType: SignatureType, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}

