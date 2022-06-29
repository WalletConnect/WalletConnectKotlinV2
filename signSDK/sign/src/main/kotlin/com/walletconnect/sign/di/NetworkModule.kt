package com.walletconnect.sign.di

import com.walletconnect.sign.network.data.service.NonceService
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@JvmSynthetic
internal fun networkModule(baseUrl: String) = module {
    val TIMEOUT_TIME = 5000L

    single {
        OkHttpClient.Builder()
            .writeTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .callTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .connectTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .build()
    }

    single<Converter.Factory> {
        MoshiConverterFactory.create(get())
    }

    single {
        Retrofit.Builder()
            .addConverterFactory(get())
            .client(get())
            .baseUrl(baseUrl)
            .build()
    }

    single {
        get<Retrofit>().create(NonceService::class.java)
    }
}