package com.walletconnect.android.internal.common.di

import com.walletconnect.android.pulse.data.PulseService
import com.walletconnect.android.pulse.domain.SendClickAllWalletsUseCase
import com.walletconnect.android.pulse.domain.SendClickGetWalletUseCase
import com.walletconnect.android.pulse.domain.SendClickNetworkHelpUseCase
import com.walletconnect.android.pulse.domain.SendClickNetworksUseCase
import com.walletconnect.android.pulse.domain.SendClickWalletHelpUseCase
import com.walletconnect.android.pulse.domain.SendConnectErrorUseCase
import com.walletconnect.android.pulse.domain.SendConnectSuccessUseCase
import com.walletconnect.android.pulse.domain.SendDisconnectErrorUseCase
import com.walletconnect.android.pulse.domain.SendDisconnectSuccessUseCase
import com.walletconnect.android.pulse.domain.SendModalCloseUseCase
import com.walletconnect.android.pulse.domain.SendModalCreatedUseCase
import com.walletconnect.android.pulse.domain.SendModalLoadedUseCase
import com.walletconnect.android.pulse.domain.SendModalLoadedUseCaseInterface
import com.walletconnect.android.pulse.domain.SendModalOpenUseCase
import com.walletconnect.android.pulse.domain.SendSelectWalletUseCase
import com.walletconnect.android.pulse.domain.SendSwitchNetworkUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
fun pulseModule(bundleId: String) = module {
    single(named(AndroidCommonDITags.PULSE_URL)) { "https://pulse.walletconnect.org" }

    single(named(AndroidCommonDITags.PULSE_RETROFIT)) {
        Retrofit.Builder()
            .baseUrl(get<String>(named(AndroidCommonDITags.PULSE_URL)))
            .client(get(named(AndroidCommonDITags.WEB3MODAL_OKHTTP)))
            .addConverterFactory(MoshiConverterFactory.create(get(named(AndroidCommonDITags.MOSHI))))
            .build()
    }

    single { get<Retrofit>(named(AndroidCommonDITags.PULSE_RETROFIT)).create(PulseService::class.java) }

    single {
        SendModalCreatedUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendClickAllWalletsUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendClickGetWalletUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendClickWalletHelpUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendClickNetworkHelpUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendClickNetworksUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendConnectErrorUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendConnectSuccessUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendDisconnectErrorUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendDisconnectSuccessUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendModalCloseUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single<SendModalLoadedUseCaseInterface> {
        SendModalLoadedUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendModalOpenUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendSelectWalletUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }

    single {
        SendSwitchNetworkUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = bundleId
        )
    }
}