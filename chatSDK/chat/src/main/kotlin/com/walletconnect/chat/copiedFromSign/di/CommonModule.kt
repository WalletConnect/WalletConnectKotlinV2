package com.walletconnect.chat.copiedFromSign.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.chat.copiedFromSign.core.adapters.JSONObjectAdapter
import com.walletconnect.chat.copiedFromSign.core.adapters.SubscriptionIdAdapter
import com.walletconnect.chat.copiedFromSign.core.adapters.TopicAdapter
import com.walletconnect.chat.copiedFromSign.core.adapters.TtlAdapter
import com.walletconnect.chat.copiedFromSign.core.model.vo.SubscriptionIdVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.TopicVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.TtlVO
import com.walletconnect.chat.copiedFromSign.json_rpc.model.RelayerDO
import org.json.JSONObject
import org.koin.dsl.module
import kotlin.reflect.jvm.jvmName

@JvmSynthetic
internal fun commonModule() = module {

    single<PolymorphicJsonAdapterFactory<RelayerDO.JsonRpcResponse>> {
        PolymorphicJsonAdapterFactory.of(RelayerDO.JsonRpcResponse::class.java, "type")
            .withSubtype(RelayerDO.JsonRpcResponse.JsonRpcResult::class.java, "result")
            .withSubtype(RelayerDO.JsonRpcResponse.JsonRpcError::class.java, "error")
    }

    single {
        KotlinJsonAdapterFactory()
    }

    single {
        Moshi.Builder()
            .addLast { type, _, moshi ->
                when (type.getRawType().name) {
//                    ExpiryVO::class.jvmName -> ExpiryAdapter
                    JSONObject::class.jvmName -> JSONObjectAdapter
                    SubscriptionIdVO::class.jvmName -> SubscriptionIdAdapter
                    TopicVO::class.jvmName -> TopicAdapter
                    TtlVO::class.jvmName -> TtlAdapter
//                    SessionRequestVO::class.jvmName -> SessionRequestVOJsonAdapter(moshi)
//                    RelayerDO.JsonRpcResponse.JsonRpcResult::class.jvmName -> RelayDOJsonRpcResultJsonAdapter(moshi)
                    else -> null
                }
            }
            .addLast(get<KotlinJsonAdapterFactory>())
            .add(get<PolymorphicJsonAdapterFactory<RelayerDO.JsonRpcResponse>>())
            .build()
    }
}