@file:JvmSynthetic

package com.walletconnect.push.wallet.di

import com.walletconnect.android.impl.di.coreStorageModule
import com.walletconnect.android.impl.di.sdkBaseStorageModule
import com.walletconnect.push.wallet.client.WalletProtocol.Companion.storageSuffix
import org.koin.dsl.module

@JvmSynthetic
internal fun pushStorageModule(storagePrefix: String) = module {

    // TODO: Add subscription storage after dapp
    includes(coreStorageModule()/*, sdkBaseStorageModule(Database.Schema, storageSuffix)*/)


}