@file:JvmSynthetic

package com.walletconnect.android.internal.common.di

import com.walletconnect.android.archive.ArchiveInterface
import com.walletconnect.android.archive.ArchiveMessageNotifier
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
internal fun archiveModule(archive: ArchiveInterface, archiveServerUrl: String? = null, timeout: NetworkClientTimeout? = null) = module {
    val archiveServerUrl = archiveServerUrl ?: DEFAULT_ARCHIVE_URL
    val networkClientTimeout = timeout ?: NetworkClientTimeout.getDefaultTimeout() // todo: maybe not use relay timeouts?

    single(named(AndroidCommonDITags.ARCHIVE_SERVER_URL)) { archiveServerUrl }

    single(named(AndroidCommonDITags.ARCHIVE_SERVER_RETROFIT)) {
        Retrofit.Builder()
            .baseUrl(archiveServerUrl)
            .client(get(named(AndroidCommonDITags.OK_HTTP)))
            .addConverterFactory(MoshiConverterFactory.create(get(named(AndroidCommonDITags.MOSHI))))
            .build()
    }

    single { get<Retrofit>(named(AndroidCommonDITags.ARCHIVE_SERVER_RETROFIT)).create(ArchiveServerService::class.java) }

    single { ArchiveMessageNotifier() }
    single { ReduceSyncRequestsUseCase(get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }
    single { RegisterTagsUseCase(get(), get(), get(named(AndroidCommonDITags.ARCHIVE_SERVER_URL)), get(named(AndroidCommonDITags.LOGGER))) }
    single { GetMessagesUseCase(get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { archive }
}

private const val DEFAULT_ARCHIVE_URL = "https://history.walletconnect.com"