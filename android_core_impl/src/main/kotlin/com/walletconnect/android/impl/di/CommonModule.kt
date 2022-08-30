package com.walletconnect.android.impl.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.impl.common.adapters.ExpiryAdapter
import com.walletconnect.android.impl.common.adapters.TagsAdapter
import com.walletconnect.android.impl.common.model.Expiry
import com.walletconnect.android.impl.common.model.type.enums.Tags
import com.walletconnect.android.impl.json_rpc.model.JsonRpc
import com.walletconnect.foundation.di.FoundationDITags
import com.walletconnect.foundation.util.Logger
import org.koin.core.qualifier.named
import org.koin.dsl.module
import timber.log.Timber
import kotlin.reflect.jvm.jvmName
import com.walletconnect.foundation.di.commonModule as foundationCommonModule

fun commonModule() = module {

    includes(foundationCommonModule())

    single<PolymorphicJsonAdapterFactory<JsonRpc.JsonRpcResponse>> {
        PolymorphicJsonAdapterFactory.of(JsonRpc.JsonRpcResponse::class.java, "type")
            .withSubtype(JsonRpc.JsonRpcResponse.JsonRpcResult::class.java, "result")
            .withSubtype(JsonRpc.JsonRpcResponse.JsonRpcError::class.java, "error")
    }

    single<Moshi>(named(AndroidCoreDITags.MOSHI)) {
        get<Moshi>(named(FoundationDITags.MOSHI))
            .newBuilder()
            .add { type, _, _ ->
                when (type.getRawType().name) {
                    Expiry::class.jvmName -> ExpiryAdapter
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
