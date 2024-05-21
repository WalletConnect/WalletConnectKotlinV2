package com.walletconnect.android.internal.common.di

import com.walletconnect.android.pulse.domain.session.SendOptionalNamespaceValidationFailureUseCase
import com.walletconnect.android.pulse.domain.session.SendProposalExpiredUseCase
import com.walletconnect.android.pulse.domain.session.SendRequiredNamespaceValidationFailureUseCase
import com.walletconnect.android.pulse.domain.session.SendSessionApproveNamespaceValidationFailureUseCase
import com.walletconnect.android.pulse.domain.session.SendSessionApprovePublishFailureUseCase
import com.walletconnect.android.pulse.domain.session.SendSessionPropertiesValidationFailureUseCase
import com.walletconnect.android.pulse.domain.session.SendSessionSettlePublishFailureUseCase
import com.walletconnect.android.pulse.domain.session.SendSessionSubscriptionFailureUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
fun sessionPulseModule(bundleId: String) = module {
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

	single {
		SendRequiredNamespaceValidationFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendOptionalNamespaceValidationFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendSessionPropertiesValidationFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}
}