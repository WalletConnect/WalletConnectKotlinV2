package com.walletconnect.android.internal.common.di

import com.squareup.moshi.Moshi
import com.walletconnect.android.pulse.data.PulseService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
fun pulseModule() = module {
	single(named(AndroidCommonDITags.PULSE_URL)) {
		//todo: use prod
//		"https://pulse.walletconnect.org"
		"https://analytics-api-cf-workers-staging.walletconnect-v1-bridge.workers.dev"
	}

	single(named(AndroidCommonDITags.PULSE_RETROFIT)) {
		Retrofit.Builder()
			.baseUrl(get<String>(named(AndroidCommonDITags.PULSE_URL)))
			.client(get(named(AndroidCommonDITags.WEB3MODAL_OKHTTP)))
			.addConverterFactory(MoshiConverterFactory.create(get<Moshi.Builder>(named(AndroidCommonDITags.MOSHI)).build()))
			.build()
	}

	single {
		get<Retrofit>(named(AndroidCommonDITags.PULSE_RETROFIT)).create(PulseService::class.java)
	}

	includes(w3mPulseModule(), pairingPulseModule(), signPulseModule())
}