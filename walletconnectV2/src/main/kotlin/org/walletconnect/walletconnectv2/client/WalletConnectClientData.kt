package org.walletconnect.walletconnectv2.client

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.net.URI
import kotlin.reflect.typeOf

sealed class WalletConnectClientData {

    data class SessionProposal(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<URI>,
        val chains: List<String>,
        var methods: List<String>,
        val topic: String,
        val proposerPublicKey: String,
        val ttl: Long
    ) {
        val icon: String = icons.first().toString()
    }

    data class SessionRequest(
        val topic: String,
        val requestStringified: String,
        val chainId: String?,
        val method: String
    ) {
//        @ExperimentalStdlibApi
//        inline fun <reified T> decode(): T? {
//            val test = listOf<Any>()
//            val tes2 = listOf<Object>()
//
//            if (typeOf<T>().classifier == List::class) {
//                return Moshi.Builder()
//                    .add(KotlinJsonAdapterFactory())
//                    .build()
//                    .adapter<T>(Types.newParameterizedType(List::class.java, subtypeOf<T>().rawType))
//                    .fromJson(requestStringified)
//            }
//            return Moshi.Builder()
//                .add(KotlinJsonAdapterFactory())
//                .build()
//                .adapter(T::class.java)
//                .lenient()
//                .fromJson(requestStringified)
//        }
    }

    data class SettledSession(
        var icon: String? = "",
        var name: String = "",
        var uri: String = "",
        val topic: String
    )
}