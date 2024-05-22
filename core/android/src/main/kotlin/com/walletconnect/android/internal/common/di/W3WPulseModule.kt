package com.walletconnect.android.internal.common.di

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

@JvmSynthetic
fun w3mPulseModule(bundleId: String) = module {
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