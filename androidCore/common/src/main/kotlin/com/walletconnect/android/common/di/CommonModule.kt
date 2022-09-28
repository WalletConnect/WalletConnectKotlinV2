package com.walletconnect.android.common.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.common.JsonRpcResponse
import com.walletconnect.android.common.adapter.ExpiryAdapter
import com.walletconnect.android.common.adapter.TagsAdapter
import com.walletconnect.android.common.model.pairing.Expiry
import com.walletconnect.android.common.model.Tags
import com.walletconnect.foundation.di.FoundationDITags
import com.walletconnect.foundation.util.Logger
import org.koin.core.qualifier.named
import org.koin.dsl.module
import timber.log.Timber
import kotlin.reflect.jvm.jvmName
import com.walletconnect.foundation.di.commonModule as foundationCommonModule

fun commonModule() = module {

    includes(foundationCommonModule())

    single<PolymorphicJsonAdapterFactory<JsonRpcResponse>> {
        PolymorphicJsonAdapterFactory.of(JsonRpcResponse::class.java, "type")
            .withSubtype(JsonRpcResponse.JsonRpcResult::class.java, "result")
            .withSubtype(JsonRpcResponse.JsonRpcError::class.java, "error")
    }

    single<Moshi>(named(AndroidCommonDITags.MOSHI)) {
        get<Moshi>(named(FoundationDITags.MOSHI))
            .newBuilder()
            .add { type, _, _ ->
                when (type.getRawType().name) {
                    Expiry::class.jvmName -> ExpiryAdapter
                    Tags::class.jvmName -> TagsAdapter
                    else -> null
                }
            }
            .add(get<PolymorphicJsonAdapterFactory<JsonRpcResponse>>())
            .build()
    }

    single<Logger>(named(AndroidCommonDITags.LOGGER)) {
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