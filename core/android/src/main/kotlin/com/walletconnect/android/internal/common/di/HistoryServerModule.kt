@file:JvmSynthetic

package com.walletconnect.android.internal.common.di

import com.walletconnect.android.archive.HistoryInterface
import com.walletconnect.android.archive.HistoryMessageNotifier
import com.walletconnect.android.archive.ReduceSyncRequestsUseCase
import com.walletconnect.android.archive.domain.GetMessagesUseCase
import com.walletconnect.android.archive.domain.RegisterTagsUseCase
import com.walletconnect.android.archive.network.ArchiveServerService
import com.walletconnect.android.relay.NetworkClientTimeout
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
internal fun historyModule(history: HistoryInterface, historyServerUrl: String? = null, timeout: NetworkClientTimeout? = null) = module {
    val historyServerUrl = historyServerUrl ?: DEFAULT_HISTORY_URL
    val networkClientTimeout = timeout ?: NetworkClientTimeout.getDefaultTimeout() // todo: maybe not use relay timeouts?

    single(named(AndroidCommonDITags.HISTORY_SERVER_URL)) { historyServerUrl }

    single(named(AndroidCommonDITags.HISTORY_SERVER_RETROFIT)) {
        Retrofit.Builder()
            .baseUrl(historyServerUrl)
            .client(get(named(AndroidCommonDITags.OK_HTTP)))
            .addConverterFactory(MoshiConverterFactory.create(get(named(AndroidCommonDITags.MOSHI))))
            .build()
    }

    single { get<Retrofit>(named(AndroidCommonDITags.HISTORY_SERVER_RETROFIT)).create(ArchiveServerService::class.java) }

    single { HistoryMessageNotifier() }
    single { ReduceSyncRequestsUseCase(get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }
    single { RegisterTagsUseCase(get(), get(), get(named(AndroidCommonDITags.HISTORY_SERVER_URL)), get(named(AndroidCommonDITags.LOGGER))) }
    single { GetMessagesUseCase(get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { history }
}

private const val DEFAULT_HISTORY_URL = "https://history.walletconnect.com"