package com.walletconnect.android.internal.common.di

import com.walletconnect.android.internal.common.model.PackageName
import com.walletconnect.android.pulse.domain.pairing.SendFailedToSubscribeToPairingTopicUseCase
import com.walletconnect.android.pulse.domain.pairing.SendMalformedPairingUriUseCase
import com.walletconnect.android.pulse.domain.pairing.SendNoInternetConnectionUseCase
import com.walletconnect.android.pulse.domain.pairing.SendNoWSSConnectionUseCase
import com.walletconnect.android.pulse.domain.pairing.SendPairingAlreadyExistUseCase
import com.walletconnect.android.pulse.domain.pairing.SendPairingExpiredUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
fun pairingPulseModule() = module {
	single {
		SendMalformedPairingUriUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendPairingAlreadyExistUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendFailedToSubscribeToPairingTopicUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendPairingExpiredUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendNoWSSConnectionUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendNoInternetConnectionUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}
}