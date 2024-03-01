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
import com.walletconnect.android.pulse.domain.SendModalOpenUseCase
import com.walletconnect.android.pulse.domain.SendSelectWalletUseCase
import com.walletconnect.android.pulse.domain.SendSwitchNetworkUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
fun pulseModule() = module {
    single(named(AndroidCommonDITags.PULSE_URL)) { "https://pulse.walletconnect.com" }

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
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendClickAllWalletsUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendClickGetWalletUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendClickWalletHelpUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendClickNetworkHelpUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendClickNetworksUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendConnectErrorUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendConnectSuccessUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendDisconnectErrorUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendDisconnectSuccessUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendModalCloseUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendModalLoadedUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendModalOpenUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendSelectWalletUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }

    single {
        SendSwitchNetworkUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID))
        )
    }
}