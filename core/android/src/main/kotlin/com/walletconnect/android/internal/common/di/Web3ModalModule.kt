package com.walletconnect.android.internal.common.di

import com.walletconnect.android.BuildConfig
import com.walletconnect.android.internal.common.modal.Web3ModalApiRepository
import com.walletconnect.android.internal.common.modal.data.network.Web3ModalService
import com.walletconnect.android.internal.common.modal.domain.usecase.GetAllWalletsUseCase
import com.walletconnect.android.internal.common.modal.domain.usecase.GetAllWalletsUseCaseInterface
import com.walletconnect.android.internal.common.model.ProjectId
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
internal fun web3ModalModule() = module {
    single(named(AndroidCommonDITags.WEB3MODAL_URL)) { "https://api.web3modal.com/" }

    single(named(AndroidCommonDITags.WEB3MODAL_INTERCEPTOR)) {
        Interceptor { chain ->
            val updatedRequest = chain.request().newBuilder()
                .addHeader("x-project-id", get<ProjectId>().value)
                .addHeader("x-sdk-version", "kotlin-${BuildConfig.SDK_VERSION}")
                .build()
            chain.proceed(updatedRequest)
        }
    }

    single(named(AndroidCommonDITags.WEB3MODAL_OKHTTP)) {
        get<OkHttpClient>(named(AndroidCommonDITags.OK_HTTP))
            .newBuilder()
            .addInterceptor(get<Interceptor>(named(AndroidCommonDITags.WEB3MODAL_INTERCEPTOR)))
            .build()
    }

    single(named(AndroidCommonDITags.WEB3MODAL_RETROFIT)) {
        Retrofit.Builder()
            .baseUrl(get<String>(named(AndroidCommonDITags.WEB3MODAL_URL)))
            .client(get(named(AndroidCommonDITags.WEB3MODAL_OKHTTP)))
            .addConverterFactory(MoshiConverterFactory.create(get(named(AndroidCommonDITags.MOSHI))))
            .build()
    }

    single { get<Retrofit>(named(AndroidCommonDITags.WEB3MODAL_RETROFIT)).create(Web3ModalService::class.java) }

    single {
        Web3ModalApiRepository(
            web3ModalApiUrl = get(named(AndroidCommonDITags.WEB3MODAL_URL)),
            web3ModalService = get(),
            context = androidContext()
        )
    }

    single<GetAllWalletsUseCaseInterface> { GetAllWalletsUseCase(web3ModalApiRepository = get()) }
}