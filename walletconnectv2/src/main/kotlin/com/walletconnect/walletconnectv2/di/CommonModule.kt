package com.walletconnect.walletconnectv2.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.walletconnectv2.core.adapters.*
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.SubscriptionIdVO
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.TtlVO
import com.walletconnect.walletconnectv2.relay.model.RelayDO
import org.json.JSONObject
import org.koin.dsl.module

@JvmSynthetic
internal fun commonModule() = module {

    single<PolymorphicJsonAdapterFactory<RelayDO.JsonRpcResponse>> {
        PolymorphicJsonAdapterFactory.of(RelayDO.JsonRpcResponse::class.java, "type")
            .withSubtype(RelayDO.JsonRpcResponse.JsonRpcResult::class.java, "result")
            .withSubtype(RelayDO.JsonRpcResponse.JsonRpcError::class.java, "error")
    }

    single {
        KotlinJsonAdapterFactory()
    }

    single {
        Moshi.Builder()
            .addLast { type, _, _ ->
                when (type.getRawType().name) {
                    ExpiryVO::class.qualifiedName -> ExpiryAdapter
                    JSONObject::class.qualifiedName -> JSONObjectAdapter
                    SubscriptionIdVO::class.qualifiedName -> SubscriptionIdAdapter
                    TopicVO::class.qualifiedName -> TopicAdapter
                    TtlVO::class.qualifiedName -> TtlAdapter
                    else -> null
                }
            }
            .addLast(get<KotlinJsonAdapterFactory>())
            .add(get<PolymorphicJsonAdapterFactory<RelayDO.JsonRpcResponse>>())
            .build()
    }
}