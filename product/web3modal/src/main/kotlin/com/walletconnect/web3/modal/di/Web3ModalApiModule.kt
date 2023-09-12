package com.walletconnect.web3.modal.di

import com.walletconnect.android.BuildConfig
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.web3.modal.data.network.Web3ModalService
import com.walletconnect.web3.modal.domain.Web3ModalApiRepository
import com.walletconnect.web3.modal.domain.usecase.GetAllWalletsUseCase
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal fun web3ModalApiModule() = module {

    single(named(Web3ModalDITags.API_URL)) { "https://api.web3modal.com/" }

    single(named(Web3ModalDITags.INTERCEPTOR)) {
        Interceptor { chain ->
            val updatedRequest = chain.request().newBuilder()
                .addHeader("x-project-id", BuildConfig.PROJECT_ID)
                .addHeader("x-sdk-version", "kotlin-${BuildConfig.SDK_VERSION}")
                .addHeader("x-sdk-type", "w3m")
                .build()
            chain.proceed(updatedRequest)
        }
    }

    single(named(Web3ModalDITags.OKHTTP)) {
        get<OkHttpClient>(named(AndroidCommonDITags.OK_HTTP))
            .newBuilder()
            .addInterceptor(get<Interceptor>(named(Web3ModalDITags.INTERCEPTOR)))
            .build()
    }

    single(named(Web3ModalDITags.RETROFIT)) {
        Retrofit.Builder()
            .baseUrl(get<String>(named(Web3ModalDITags.API_URL)))
            .client(get(named(Web3ModalDITags.OKHTTP)))
            .addConverterFactory(MoshiConverterFactory.create(get(named(AndroidCommonDITags.MOSHI))))
            .build()
    }

    single { get<Retrofit>(named(Web3ModalDITags.RETROFIT)).create(Web3ModalService::class.java) }

    single {
        Web3ModalApiRepository(
            web3ModalApiUrl = get(named(Web3ModalDITags.API_URL)),
            web3ModalService = get()
        )
    }

    single { GetAllWalletsUseCase(web3ModalApiRepository = get()) }

}