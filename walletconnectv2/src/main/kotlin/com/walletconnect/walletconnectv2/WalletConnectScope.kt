@file:JvmName("WalletConnectScope")

package com.walletconnect.walletconnectv2

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.json.JSONObject
import com.walletconnect.walletconnectv2.common.Expiry
import com.walletconnect.walletconnectv2.common.SubscriptionId
import com.walletconnect.walletconnectv2.common.Topic
import com.walletconnect.walletconnectv2.common.Ttl
import com.walletconnect.walletconnectv2.common.network.adapters.*
import com.walletconnect.walletconnectv2.jsonrpc.model.JsonRpcResponse

//TODO add job cancellation to avoid memory leaks
internal lateinit var app: Application
private val job = SupervisorJob()
internal val scope = CoroutineScope(job + Dispatchers.IO)

private val polymorphicJsonAdapterFactory: PolymorphicJsonAdapterFactory<JsonRpcResponse> =
    PolymorphicJsonAdapterFactory.of(JsonRpcResponse::class.java, "type")
        .withSubtype(JsonRpcResponse.JsonRpcResult::class.java, "result")
        .withSubtype(JsonRpcResponse.JsonRpcError::class.java, "error")

//TODO move to the DI framework
val moshi: Moshi = Moshi.Builder()
    .addLast { type, _, _ ->
        when (type.getRawType().name) {
            Expiry::class.qualifiedName -> ExpiryAdapter
            JSONObject::class.qualifiedName -> JSONObjectAdapter
            SubscriptionId::class.qualifiedName -> SubscriptionIdAdapter
            Topic::class.qualifiedName -> TopicAdapter
            Ttl::class.qualifiedName -> TtlAdapter
            else -> null
        }
    }
    .addLast(KotlinJsonAdapterFactory())
    .add(polymorphicJsonAdapterFactory)
    .build()