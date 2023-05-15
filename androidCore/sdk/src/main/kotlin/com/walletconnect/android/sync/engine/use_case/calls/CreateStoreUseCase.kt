package com.walletconnect.android.sync.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.sync.common.exception.validateAccountId
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.android.sync.engine.use_case.subscriptions.SubscribeToStoreUpdatesUseCase
import com.walletconnect.android.sync.storage.AccountsStorageRepository
import com.walletconnect.android.sync.storage.StoresStorageRepository
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.util.bytesToHex
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.kethereum.bip39.entropyToMnemonic
import org.kethereum.bip39.model.MnemonicWords
import org.kethereum.bip39.toKey
import org.kethereum.bip39.wordlists.WORDLIST_ENGLISH

internal class CreateStoreUseCase(
    private val accountsRepository: AccountsStorageRepository,
    private val storesRepository: StoresStorageRepository,
    private val subscribeToStoreUpdatesUseCase: SubscribeToStoreUpdatesUseCase,
    private val keyManagementRepository: KeyManagementRepository,
) : CreateUseCaseInterface {

    // note(Szymon): generation of symmetric keys for store topic must not be done in parallel
    private val mutex = Mutex()

    override fun create(accountId: AccountId, store: Store, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                mutex.withLock {
                    validateAccountId(accountId) { error -> return@withLock onFailure(error) }

                    // When store exists then return call onSuccess() and finish use case invocation
                    if (!storesRepository.doesStoreNotExists(accountId, store)) return@withLock onSuccess()

                    val keyPath = store.getDerivationPath()
                    val entropy = runCatching { accountsRepository.getAccount(accountId).entropy }.getOrElse { error -> return@withLock onFailure(error) }
                    val mnemonic = MnemonicWords(entropyToMnemonic(entropy.toBytes(), WORDLIST_ENGLISH))
                    val storeSymmetricKey = SymmetricKey(mnemonic.toKey(keyPath).keyPair.privateKey.key.toByteArray().bytesToHex().takeLast(64))
                    val storeTopic = Topic(sha256(storeSymmetricKey.keyAsBytes))

                    keyManagementRepository.setKey(storeSymmetricKey, storeTopic.value)

                    runCatching { storesRepository.createStore(accountId, store, storeSymmetricKey, storeTopic) }.fold(
                        onSuccess = { subscribeToStoreUpdatesUseCase(storeTopic, onSuccess = { onSuccess() }, onError = { error -> onFailure(error) }) },
                        onFailure = { error -> onFailure(error) }
                    )
                }
            }
        }
    }
}

internal interface CreateUseCaseInterface {
    fun create(accountId: AccountId, store: Store, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}
