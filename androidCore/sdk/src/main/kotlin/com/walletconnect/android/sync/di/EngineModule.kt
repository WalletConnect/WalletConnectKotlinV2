@file:JvmSynthetic

package com.walletconnect.android.sync.di


import com.walletconnect.android.internal.common.signing.message.MessageSignatureVerifier
import com.walletconnect.android.sync.engine.domain.SyncEngine
import com.walletconnect.android.sync.engine.use_case.calls.*
import com.walletconnect.android.sync.engine.use_case.requests.OnDeleteRequestUseCase
import com.walletconnect.android.sync.engine.use_case.requests.OnSetRequestUseCase
import com.walletconnect.android.sync.engine.use_case.responses.OnDeleteResponseUseCase
import com.walletconnect.android.sync.engine.use_case.responses.OnSetResponseUseCase
import com.walletconnect.android.sync.engine.use_case.subscriptions.SubscribeToAllStoresUpdatesUseCase
import com.walletconnect.android.sync.engine.use_case.subscriptions.SubscribeToStoreUpdatesUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {
    single { MessageSignatureVerifier(get()) }

    single { CreateStoreUseCase(get(), get(), get(), get()) }
    single { DeleteStoreValueUseCase(get(), get()) }
    single { SetStoreValueUseCase(get(), get()) }
    single { GetStoresUseCase(get()) }
    single { RegisterAccountUseCase(get(), get()) }

    single { OnSetRequestUseCase(get(), get(), get()) }
    single { OnDeleteRequestUseCase(get(), get(), get()) }

    single { OnSetResponseUseCase(get()) }
    single { OnDeleteResponseUseCase(get()) }

    single { SubscribeToStoreUpdatesUseCase(get(), get()) }
    single { SubscribeToAllStoresUpdatesUseCase(get(), get(), get()) }

    single {
        SyncEngine(
            getStoresUseCase = get(),
            registerAccountUseCase = get(),
            createStoreUseCase = get(),
            deleteStoreValueUseCase = get(),
            setStoreValueUseCase = get(),
            pairingHandler = get(),
            jsonRpcInteractor = get(),
            onSetRequestUseCase = get(),
            onDeleteRequestUseCase = get(),
            onSetResponseUseCase = get(),
            onDeleteResponseUseCase = get(),
            subscribeToAllStoresUpdatesUseCase = get(),
            logger = get()
        )
    }
}