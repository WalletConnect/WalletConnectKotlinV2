@file:JvmSynthetic

package com.walletconnect.android.echo

import android.content.SharedPreferences
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.wcKoinApp
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal object EchoClient: EchoInterface {

    override fun initialize() {
        wcKoinApp.modules(
            module {
                single {
                    Retrofit.Builder()
                        .baseUrl("https://echo.walletconnect.com/")
                        .addConverterFactory(MoshiConverterFactory.create())
                        .client(wcKoinApp.koin.get(named(AndroidCommonDITags.OK_HTTP)))
                        .build()
                        .create(EchoService::class.java)
                }
            }
        )
    }

    override fun register(firebaseAccessToken: String) {
        val echoService = wcKoinApp.koin.get<EchoService>()
        val sharedPreferences = wcKoinApp.koin.get<SharedPreferences>()


    }

    override fun unregister() {

    }

    override fun decryptMessage(topic: String, message: String): String {
        return ""
    }
}