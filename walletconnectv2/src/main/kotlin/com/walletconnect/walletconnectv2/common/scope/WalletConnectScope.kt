@file:JvmName("WalletConnectScope")

package com.walletconnect.walletconnectv2.common.scope

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.walletconnectv2.common.adapters.*
import com.walletconnect.walletconnectv2.common.model.vo.*
import com.walletconnect.walletconnectv2.relay.model.RelayDO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.json.JSONObject

//TODO add job cancellation to avoid memory leaks
internal lateinit var app: Application
private val job = SupervisorJob()
internal val scope = CoroutineScope(job + Dispatchers.IO)

private val polymorphicJsonAdapterFactory: PolymorphicJsonAdapterFactory<RelayDO.JsonRpcResponse> =
    PolymorphicJsonAdapterFactory.of(RelayDO.JsonRpcResponse::class.java, "type")
        .withSubtype(RelayDO.JsonRpcResponse.JsonRpcResult::class.java, "result")
        .withSubtype(RelayDO.JsonRpcResponse.JsonRpcError::class.java, "error")

//TODO move to the DI framework
val moshi: Moshi = Moshi.Builder()
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
    .addLast(KotlinJsonAdapterFactory())
    .add(polymorphicJsonAdapterFactory)
    .build()