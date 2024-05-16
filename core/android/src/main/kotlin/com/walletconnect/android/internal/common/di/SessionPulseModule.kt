package com.walletconnect.android.internal.common.di

import com.walletconnect.android.internal.common.model.PackageName
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
fun sessionPulseModule() = module {
	single {
		SendProposalExpiredUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendSessionSubscriptionFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendSessionApprovePublishFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendSessionSettlePublishFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendSessionApprovePublishFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendSessionApproveNamespaceValidationFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendRequiredNamespaceValidationFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendOptionalNamespaceValidationFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}

	single {
		SendSessionPropertiesValidationFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = get<PackageName>(named(AndroidCommonDITags.BUNDLE_ID)).value
		)
	}
}