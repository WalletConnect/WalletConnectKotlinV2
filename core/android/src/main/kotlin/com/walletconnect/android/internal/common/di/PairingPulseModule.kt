package com.walletconnect.android.internal.common.di

import com.walletconnect.android.pulse.domain.pairing.SendMalformedPairingUriUseCase
import com.walletconnect.android.pulse.domain.pairing.SendNoInternetConnectionUseCase
import com.walletconnect.android.pulse.domain.pairing.SendNoWSSConnectionUseCase
import com.walletconnect.android.pulse.domain.pairing.SendPairingAlreadyExistUseCase
import com.walletconnect.android.pulse.domain.pairing.SendPairingExpiredUseCase
import com.walletconnect.android.pulse.domain.pairing.SendPairingSubscriptionFailureUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
fun pairingPulseModule(bundleId: String) = module {
	single {
		SendMalformedPairingUriUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendPairingAlreadyExistUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendPairingSubscriptionFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendPairingExpiredUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendNoWSSConnectionUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendNoInternetConnectionUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}
}