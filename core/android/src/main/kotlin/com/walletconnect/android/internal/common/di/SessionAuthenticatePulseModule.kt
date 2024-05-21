package com.walletconnect.android.internal.common.di

import com.walletconnect.android.pulse.domain.session_authenticate.SendAuthenticatedSessionApprovePublishFailureUseCase
import com.walletconnect.android.pulse.domain.session_authenticate.SendAuthenticatedSessionExpiredUseCase
import com.walletconnect.android.pulse.domain.session_authenticate.SendChainsCaip2CompliantFailureUseCase
import com.walletconnect.android.pulse.domain.session_authenticate.SendChainsEvmCompliantFailureUseCase
import com.walletconnect.android.pulse.domain.session_authenticate.SendInvalidCacaoUseCase
import com.walletconnect.android.pulse.domain.session_authenticate.SendMissingSessionAuthenticateRequestUseCase
import com.walletconnect.android.pulse.domain.session_authenticate.SendSessionAuthenticateRequestExpiredUseCase
import com.walletconnect.android.pulse.domain.session_authenticate.SendSubscribeAuthenticatedSessionTopicFailureUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
fun sessionAuthenticatePulseModule(bundleId: String) = module {
	single {
		SendChainsCaip2CompliantFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendAuthenticatedSessionApprovePublishFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendChainsEvmCompliantFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendInvalidCacaoUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendMissingSessionAuthenticateRequestUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendSessionAuthenticateRequestExpiredUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendSubscribeAuthenticatedSessionTopicFailureUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}

	single {
		SendAuthenticatedSessionExpiredUseCase(
			pulseService = get(),
			logger = get(named(AndroidCommonDITags.LOGGER)),
			bundleId = bundleId
		)
	}
}