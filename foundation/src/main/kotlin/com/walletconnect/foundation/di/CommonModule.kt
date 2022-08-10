package com.walletconnect.foundation.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.foundation.common.adapters.SubscriptionIdAdapter
import com.walletconnect.foundation.common.adapters.TopicAdapter
import com.walletconnect.foundation.common.adapters.TtlAdapter
import com.walletconnect.foundation.common.model.SubscriptionId
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.jvm.jvmName

fun commonModule() = module {

    single {
        KotlinJsonAdapterFactory()
    }

    single(named("foundation")) {
        Moshi.Builder()
            .add { type, _, moshi ->
                when (type.getRawType().name) {
                    SubscriptionId::class.jvmName -> SubscriptionIdAdapter
                    Topic::class.jvmName -> TopicAdapter
                    Ttl::class.jvmName -> TtlAdapter
                    else -> null
                }
            }
            .addLast(get<KotlinJsonAdapterFactory>())
            .build()
    }

    single {
        object: Logger {
            override fun log(logMsg: String?) {
                println(logMsg)
            }

            override fun log(throwable: Throwable?) {
                println(throwable?.stackTraceToString() ?: throwable?.message!!)
            }

            override fun error(errorMsg: String?) {
                println(errorMsg)
            }

            override fun error(throwable: Throwable?) {
                println(throwable?.stackTraceToString() ?: throwable?.message!!)
            }
        }
    }
}