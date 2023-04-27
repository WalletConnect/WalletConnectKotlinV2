package com.walletconnect.web3.modal.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.web3.modal.data.explorer.ExplorerMapToListAdapter
import com.walletconnect.web3.modal.data.explorer.ExplorerRepository
import com.walletconnect.web3.modal.domain.configuration.EncodedStringAdapter
import com.walletconnect.web3.modal.domain.configuration.configAdapter
import com.walletconnect.web3.modal.domain.usecases.GetWalletsUseCase
import com.walletconnect.web3.modal.domain.usecases.GetWalletsUseCaseImpl
import com.walletconnect.web3.modal.network.ExplorerService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val EXPLORER_RETROFIT = "explorer_retrofit"
internal const val WEB3MODAL_MOSHI = "web3modal_moshi"

internal fun web3ModalModule() = module {

    single(named(WEB3MODAL_MOSHI)) {
        Moshi.Builder()
            .add(configAdapter())
            .add(EncodedStringAdapter())
            .add(ExplorerMapToListAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    single(named(EXPLORER_RETROFIT)) {
        Retrofit.Builder()
            .baseUrl("https://explorer-api.walletconnect.com/")
            .addConverterFactory(MoshiConverterFactory.create(get(named(WEB3MODAL_MOSHI))))
            .client(get(named(AndroidCommonDITags.OK_HTTP)))
            .build()
    }

    single {
        get<Retrofit>(named(EXPLORER_RETROFIT))
            .create(ExplorerService::class.java)
    }

    single { ExplorerRepository(get()) }

    factory<GetWalletsUseCase> { GetWalletsUseCaseImpl(get()) }
}

