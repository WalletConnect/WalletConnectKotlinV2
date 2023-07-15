@file:JvmSynthetic

package com.walletconnect.android.sync.di


import com.walletconnect.android.internal.common.signing.message.MessageSignatureVerifier
import com.walletconnect.android.sync.engine.domain.SyncEngine
import com.walletconnect.android.sync.engine.use_case.calls.CreateStoreUseCase
import com.walletconnect.android.sync.engine.use_case.calls.DeleteStoreValueUseCase
import com.walletconnect.android.sync.engine.use_case.calls.GetStoreTopicUseCase
import com.walletconnect.android.sync.engine.use_case.calls.GetStoresUseCase
import com.walletconnect.android.sync.engine.use_case.calls.IsAccountRegisteredUseCase
import com.walletconnect.android.sync.engine.use_case.calls.RegisterAccountUseCase
import com.walletconnect.android.sync.engine.use_case.calls.SetStoreValueUseCase
import com.walletconnect.android.sync.engine.use_case.requests.OnDeleteRequestUseCase
import com.walletconnect.android.sync.engine.use_case.requests.OnSetRequestUseCase
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
    single { GetStoreTopicUseCase(get()) }
    single { RegisterAccountUseCase(get(), get()) }
    single { IsAccountRegisteredUseCase(get()) }

    single { OnSetRequestUseCase(get(), get()) }
    single { OnDeleteRequestUseCase(get(), get()) }

    single { SubscribeToStoreUpdatesUseCase(get(), get()) }
    single { SubscribeToAllStoresUpdatesUseCase(get(), get(), get()) }

    single {
        SyncEngine(
            getStoresUseCase = get(),
            getStoreTopicUseCase = get(),
            registerAccountUseCase = get(),
            isAccountRegisteredUseCase = get(),
            createStoreUseCase = get(),
            deleteStoreValueUseCase = get(),
            setStoreValueUseCase = get(),
            pairingHandler = get(),
            jsonRpcInteractor = get(),
            onSetRequestUseCase = get(),
            onDeleteRequestUseCase = get(),
            subscribeToAllStoresUpdatesUseCase = get(),
            historyInterface = get(),
        )
    }
}