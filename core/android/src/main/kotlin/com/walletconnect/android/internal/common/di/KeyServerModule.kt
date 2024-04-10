@file:JvmSynthetic

package com.walletconnect.android.internal.common.di

import com.walletconnect.android.keyserver.data.service.KeyServerService
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.android.keyserver.domain.use_case.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
internal fun keyServerModule(optionalKeyServerUrl: String? = null) = module {
    val keyServerUrl = optionalKeyServerUrl ?: DEFAULT_KEYSERVER_URL

    single(named(AndroidCommonDITags.KEYSERVER_URL)) { keyServerUrl }

    single(named(AndroidCommonDITags.KEYSERVER_RETROFIT)) {
        Retrofit.Builder()
            .baseUrl(keyServerUrl)
            .client(get(named(AndroidCommonDITags.OK_HTTP)))
            .addConverterFactory(MoshiConverterFactory.create(get(named(AndroidCommonDITags.MOSHI))))
            .build()
    }

    single { get<Retrofit>(named(AndroidCommonDITags.KEYSERVER_RETROFIT)).create(KeyServerService::class.java) }

    single { RegisterIdentityUseCase(service = get()) }
    single { UnregisterIdentityUseCase(service = get()) }
    single { ResolveIdentityUseCase(service = get()) }
    single { RegisterInviteUseCase(service = get()) }
    single { UnregisterInviteUseCase(service = get()) }
    single { ResolveInviteUseCase(service = get()) }

    single { IdentitiesInteractor(
        identitiesRepository = get(),
        resolveIdentityUseCase = get(),
        registerIdentityUseCase = get(),
        unregisterIdentityUseCase = get(),
        projectId = get(),
        keyManagementRepository = get()
    ) }
}

private const val DEFAULT_KEYSERVER_URL = "https://keys.walletconnect.com"