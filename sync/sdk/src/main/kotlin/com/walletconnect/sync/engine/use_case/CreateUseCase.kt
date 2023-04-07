package com.walletconnect.sync.engine.use_case

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.scope
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sync.common.model.Store
import com.walletconnect.sync.storage.AccountsStorageRepository
import com.walletconnect.sync.storage.StoresStorageRepository
import com.walletconnect.util.bytesToHex
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.kethereum.bip39.entropyToMnemonic
import org.kethereum.bip39.model.MnemonicWords
import org.kethereum.bip39.toKey
import org.kethereum.bip39.wordlists.WORDLIST_ENGLISH

internal class CreateUseCase(private val accountsRepository: AccountsStorageRepository, private val storesRepository: StoresStorageRepository) : CreateUseCaseInterface {

    override fun create(accountId: AccountId, store: Store, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                val keyPath = store.getDerivationPath()
                val entropy = runCatching { accountsRepository.getAccount(accountId).entropy }.getOrElse { error -> return@supervisorScope onFailure(error) }
                val mnemonic = MnemonicWords(entropyToMnemonic(entropy.toBytes(), WORDLIST_ENGLISH))
                val storeSymmetricKey = SymmetricKey(mnemonic.toKey(keyPath).keyPair.privateKey.key.toByteArray().bytesToHex())
                val storeTopic = Topic(sha256(storeSymmetricKey.keyAsBytes))

                runCatching { storesRepository.createStore(accountId, store, storeSymmetricKey, storeTopic) }.fold(
                    onSuccess = { onSuccess() },
                    onFailure = { error -> onFailure(error) }
                )
            }
        }
    }
}

internal interface CreateUseCaseInterface {
    fun create(accountId: AccountId, store: Store, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}
