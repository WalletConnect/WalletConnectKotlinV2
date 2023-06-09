@file:JvmSynthetic

package com.walletconnect.push.wallet.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.push.wallet.engine.PushWalletEngine
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun walletEngineModule() = module {

    single {
        PushWalletEngine(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            jsonRpcInteractor = get(),
            crypto = get(),
            pairingHandler = get(),
            subscriptionStorageRepository = get(),
            messagesRepository = get(),
            identitiesInteractor = get(),
            serializer = get(),
            explorerRepository = get(),
            extractPushConfigUseCase = get(),
            logger = get()
        )
    }
}