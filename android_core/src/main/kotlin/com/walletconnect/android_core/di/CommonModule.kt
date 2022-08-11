package com.walletconnect.android_core.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android_core.common.adapters.*
import com.walletconnect.android_core.common.adapters.ExpiryAdapter
import com.walletconnect.android_core.common.adapters.JSONObjectAdapter
import com.walletconnect.android_core.common.adapters.TagsAdapter
import com.walletconnect.android_core.common.model.Expiry
import com.walletconnect.android_core.common.model.type.enums.Tags
import com.walletconnect.android_core.json_rpc.model.JsonRpc
import com.walletconnect.foundation.util.Logger
import org.json.JSONObject
import org.koin.core.qualifier.named
import org.koin.dsl.module
import timber.log.Timber
import kotlin.reflect.jvm.jvmName

fun commonModule() = module {

    single {
        KotlinJsonAdapterFactory()
    }

    single<PolymorphicJsonAdapterFactory<JsonRpc.JsonRpcResponse>> {
        PolymorphicJsonAdapterFactory.of(JsonRpc.JsonRpcResponse::class.java, "type")
            .withSubtype(JsonRpc.JsonRpcResponse.JsonRpcResult::class.java, "result")
            .withSubtype(JsonRpc.JsonRpcResponse.JsonRpcError::class.java, "error")
    }

    single {
        get<Moshi>(named("foundation"))
            .newBuilder()
            .addLast { type, _, moshi ->
                when (type.getRawType().name) {
                    Expiry::class.jvmName -> ExpiryAdapter
                    JSONObject::class.jvmName -> JSONObjectAdapter
                    Tags::class.jvmName -> TagsAdapter
                    else -> null
                }
            }
            .add(get<PolymorphicJsonAdapterFactory<JsonRpc.JsonRpcResponse>>())
            .build()
    }

    single<Logger> {
        object : Logger {
            override fun log(logMsg: String?) {
                Timber.d(logMsg)
            }

            override fun log(throwable: Throwable?) {
                Timber.d(throwable)
            }

            override fun error(errorMsg: String?) {
                Timber.e(errorMsg)
            }

            override fun error(throwable: Throwable?) {
                Timber.e(throwable)
            }
        }
    }
}
