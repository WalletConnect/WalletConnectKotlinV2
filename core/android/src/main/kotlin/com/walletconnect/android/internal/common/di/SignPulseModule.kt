package com.walletconnect.android.internal.common.di

import com.walletconnect.android.pulse.domain.sign.SendProposalExpiredUseCase
import com.walletconnect.android.pulse.domain.sign.SendSessionApproveNamespaceValidationFailureUseCase
import com.walletconnect.android.pulse.domain.sign.SendSessionApprovePublishFailureUseCase
import com.walletconnect.android.pulse.domain.sign.SendSessionSettlePublishFailureUseCase
import com.walletconnect.android.pulse.domain.sign.SendSessionSubscriptionFailureUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
fun signPulseModule(bundleId: String) = module {
	single {
		SendProposalExpiredUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendSessionSubscriptionFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendSessionApprovePublishFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendSessionSettlePublishFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendSessionApprovePublishFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendSessionApproveNamespaceValidationFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}
}