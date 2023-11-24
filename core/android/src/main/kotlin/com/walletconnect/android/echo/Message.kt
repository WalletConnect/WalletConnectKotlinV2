package com.walletconnect.android.echo

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Namespace

sealed class Message {

    data class Simple(
        val title: String,
        val body: String
    ) : Message()

    data class Notify(
        val title: String,
        val body: String,
        val icon: String?,
        val url: String?,
        val type: String
    ) : Message()

    data class SessionProposal(
        val id: Long,
        val pairingTopic: String,
        val name: String,
        val description: String,
        val url: String,
        val icons: List<String>,
        val redirect: String,
        val requiredNamespaces: Map<String, Namespace.Proposal>,
        val optionalNamespaces: Map<String, Namespace.Proposal>,
        val properties: Map<String, String>?,
        val proposerPublicKey: String,
        val relayProtocol: String,
        val relayData: String?,
    ) : Message()

    data class SessionRequest(
        val topic: String,
        val chainId: String?,
        val peerMetaData: AppMetaData?,
        val request: JSONRPCRequest,
    ) : Message() {
        data class JSONRPCRequest(
            val id: Long,
            val method: String,
            val params: String,
        ) : Message()
    }

    data class AuthRequest(
        val id: Long,
        val pairingTopic: String,
        val payloadParams: PayloadParams,
    ) : Message() {
        data class PayloadParams(
            val type: String,
            val chainId: String,
            val domain: String,
            val aud: String,
            val version: String,
            val nonce: String,
            val iat: String,
            val nbf: String?,
            val exp: String?,
            val statement: String?,
            val requestId: String?,
            val resources: List<String>?,
        )
    }
}