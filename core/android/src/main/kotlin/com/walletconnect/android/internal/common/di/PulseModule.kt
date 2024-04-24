package com.walletconnect.android.internal.common.di

import com.walletconnect.android.internal.common.model.PackageName
import com.walletconnect.android.pulse.data.PulseService
import com.walletconnect.android.pulse.domain.w3m.SendClickAllWalletsUseCase
import com.walletconnect.android.pulse.domain.w3m.SendClickGetWalletUseCase
import com.walletconnect.android.pulse.domain.w3m.SendClickNetworkHelpUseCase
import com.walletconnect.android.pulse.domain.w3m.SendClickNetworksUseCase
import com.walletconnect.android.pulse.domain.w3m.SendClickWalletHelpUseCase
import com.walletconnect.android.pulse.domain.w3m.SendConnectErrorUseCase
import com.walletconnect.android.pulse.domain.w3m.SendConnectSuccessUseCase
import com.walletconnect.android.pulse.domain.w3m.SendDisconnectErrorUseCase
import com.walletconnect.android.pulse.domain.w3m.SendDisconnectSuccessUseCase
import com.walletconnect.android.pulse.domain.w3m.SendModalCloseUseCase
import com.walletconnect.android.pulse.domain.w3m.SendModalCreatedUseCase
import com.walletconnect.android.pulse.domain.w3m.SendModalLoadedUseCase
import com.walletconnect.android.pulse.domain.w3m.SendModalLoadedUseCaseInterface
import com.walletconnect.android.pulse.domain.w3m.SendModalOpenUseCase
import com.walletconnect.android.pulse.domain.w3m.SendSelectWalletUseCase
import com.walletconnect.android.pulse.domain.w3m.SendSwitchNetworkUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
fun pulseModule() = module {
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
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendClickAllWalletsUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendClickGetWalletUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendClickWalletHelpUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendClickNetworkHelpUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendClickNetworksUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendConnectErrorUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendConnectSuccessUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendDisconnectErrorUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendDisconnectSuccessUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendModalCloseUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single<SendModalLoadedUseCaseInterface> {
        SendModalLoadedUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendModalOpenUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendSelectWalletUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }

    single {
        SendSwitchNetworkUseCase(
            pulseService = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
        )
    }
}