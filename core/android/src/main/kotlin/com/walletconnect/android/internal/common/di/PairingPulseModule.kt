package com.walletconnect.android.internal.common.di

import com.walletconnect.android.internal.common.model.PackageName
import com.walletconnect.android.pulse.domain.pairing.SendMalformedPairingUriUseCase
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
}