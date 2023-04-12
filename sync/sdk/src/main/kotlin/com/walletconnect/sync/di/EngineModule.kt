@file:JvmSynthetic

package com.walletconnect.sync.di


import com.walletconnect.android.internal.common.signing.message.MessageSignatureVerifier
import com.walletconnect.sync.engine.domain.SyncEngine
import com.walletconnect.sync.engine.use_case.calls.*
import com.walletconnect.sync.engine.use_case.subscriptions.SubscribeToAllStoresUpdatesUseCase
import com.walletconnect.sync.engine.use_case.subscriptions.SubscribeToStoreUpdatesUseCase
import com.walletconnect.sync.engine.use_case.requests.incoming.OnDeleteRequestUseCase
import com.walletconnect.sync.engine.use_case.requests.incoming.OnSetRequestUseCase
import com.walletconnect.sync.engine.use_case.requests.outgoing.SendSetRequestUseCase
import com.walletconnect.sync.engine.use_case.responses.OnDeleteResponseUseCase
import com.walletconnect.sync.engine.use_case.responses.OnSetResponseUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {
    single { MessageSignatureVerifier(get()) }

    single { CreateStoreUseCase(get(), get(), get()) }
    single { DeleteStoreValueUseCase(get()) }
    single { SetStoreValueUseCase(get(), get()) }
    single { GetStoresUseCase(get()) }
    single { RegisterAccountUseCase(get(), get()) }

    single { OnSetRequestUseCase(get(), get()) }
    single { OnDeleteRequestUseCase(get(), get()) }

    single { SendSetRequestUseCase(get(), get()) }

    single { OnSetResponseUseCase() }
    single { OnDeleteResponseUseCase() }

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
        )
    }
}