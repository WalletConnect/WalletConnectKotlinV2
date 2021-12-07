@file:JvmName("WalletConnectScope")

package org.walletconnect.walletconnectv2

import android.app.Application
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
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
import org.walletconnect.walletconnectv2.engine.model.EngineData
import org.walletconnect.walletconnectv2.util.Logger

//TODO add job cancellation to avoid memory leaks
internal lateinit var app: Application
private val job = SupervisorJob()
internal val scope = CoroutineScope(job + Dispatchers.IO)

internal val exceptionHandler = CoroutineExceptionHandler { _, exception ->
    Logger.error(exception)
}

private val polymorphicJsonAdapterFactory: PolymorphicJsonAdapterFactory<EngineData.JsonRpcResponse> =
    PolymorphicJsonAdapterFactory.of(EngineData.JsonRpcResponse::class.java, "type")
        .withSubtype(EngineData.JsonRpcResponse.JsonRpcResult::class.java, "result")
        .withSubtype(EngineData.JsonRpcResponse.JsonRpcError::class.java, "error")

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

//TODO move to the DI framework
private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
private val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
private const val SHARED_PREF_NAME: String = "wc_key_store"

internal val sharedPreferences: SharedPreferences by lazy {
    EncryptedSharedPreferences.create(
        SHARED_PREF_NAME,
        mainKeyAlias,
        app.applicationContext,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}