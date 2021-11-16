package org.walletconnect.walletconnectv2.client

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.net.URI

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
        val request: String,
        val chainId: String?,
        val method: String
    ) {
        inline fun <reified T> decode(): T? {
            return Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .adapter(T::class.java)
                .lenient()
                .fromJson(request)
        //try {
//            } catch (exception: JsonDataException) {
//                null
//            }
        }
    }

    data class SettledSession(
        var icon: String? = "",
        var name: String = "",
        var uri: String = "",
        val topic: String
    )
}