package com.walletconnect.foundation.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.dsl.module

fun commonModule() = module {

    single {
        KotlinJsonAdapterFactory()
    }

    single {
        Moshi.Builder()
            .addLast(get<KotlinJsonAdapterFactory>())
            .build()
    }
}