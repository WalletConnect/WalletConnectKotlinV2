package com.walletconnect.android.di

import com.walletconnect.android.di.coreStorageModule
import com.walletconnect.android.internal.common.di.coreCryptoModule
import com.walletconnect.android.internal.common.di.coreJsonRpcModule
import com.walletconnect.android.internal.common.di.corePairingModule
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.relay.RelayConnectionInterface
import org.koin.dsl.module

private const val SHARED_PREFS_FILE = "wc_key_store"
private const val KEY_STORE_ALIAS = "wc_keystore_key"

// When called more that once, different `storagePrefix` must be defined.
@JvmSynthetic
internal fun overrideModule(relay: RelayConnectionInterface, pairing: PairingInterface, pairingController: PairingControllerInterface, storagePrefix: String = "test_") = module {
    val sharedPrefsFile = storagePrefix + SHARED_PREFS_FILE
    val keyStoreAlias = storagePrefix + KEY_STORE_ALIAS

    single { relay }

    includes(
        coreStorageModule(storagePrefix),
        corePairingModule(pairing, pairingController),
        coreCryptoModule(sharedPrefsFile, keyStoreAlias),
        coreJsonRpcModule()
    )
}