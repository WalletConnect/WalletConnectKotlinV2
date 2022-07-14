package com.walletconnect.sign.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.sign.core.adapters.*
import com.walletconnect.sign.core.model.type.enums.Tags
import com.walletconnect.sign.core.model.vo.ExpiryVO
import com.walletconnect.sign.core.model.vo.SubscriptionIdVO
import com.walletconnect.sign.core.model.vo.TopicVO
import com.walletconnect.sign.core.model.vo.TtlVO
import com.walletconnect.sign.core.model.vo.clientsync.session.payload.SessionRequestVO
import com.walletconnect.sign.json_rpc.model.RelayerDO
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
                    ExpiryVO::class.jvmName -> ExpiryAdapter
                    JSONObject::class.jvmName -> JSONObjectAdapter
                    SubscriptionIdVO::class.jvmName -> SubscriptionIdAdapter
                    TopicVO::class.jvmName -> TopicAdapter
                    TtlVO::class.jvmName -> TtlAdapter
                    Tags::class.jvmName -> TagsAdapter
                    SessionRequestVO::class.jvmName -> SessionRequestVOJsonAdapter(moshi)
                    RelayerDO.JsonRpcResponse.JsonRpcResult::class.jvmName -> RelayDOJsonRpcResultJsonAdapter(moshi)
                    else -> null
                }
            }
            .add(get<PolymorphicJsonAdapterFactory<RelayerDO.JsonRpcResponse>>())
            .addLast(get<KotlinJsonAdapterFactory>())
            .build()
    }
}