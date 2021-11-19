@file:JvmName("WalletConnectScope")

package org.walletconnect.walletconnectv2

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.json.JSONObject
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.SubscriptionId
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.Ttl
import org.walletconnect.walletconnectv2.common.network.adapters.*
import org.walletconnect.walletconnectv2.crypto.KeyChain
import org.walletconnect.walletconnectv2.util.Logger

//TODO add job cancellation to avoid memory leaks
private val job = SupervisorJob()
internal val scope = CoroutineScope(job + Dispatchers.IO)

internal val exceptionHandler = CoroutineExceptionHandler { _, exception ->
    Logger.error(exception)
}

//TODO provide with DI
internal val keyChain = object : KeyChain {
    val mapOfKeys = mutableMapOf<String, String>()

    override fun setKey(key: String, value: String) {
        mapOfKeys[key] = value
    }

    override fun getKey(key: String): String {
        return mapOfKeys[key]!!
    }
}

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
    .build()