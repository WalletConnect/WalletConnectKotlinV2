@file:JvmSynthetic

package com.walletconnect.sign.engine.model

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.Sequence
import com.walletconnect.foundation.common.model.Topic
import java.net.URI
import com.walletconnect.android.internal.common.model.RelayProtocolOptions as CoreRelayProtocolOptions

internal sealed class EngineDO {

    class WalletConnectUri(
        val topic: Topic,
        val symKey: SymmetricKey,
        val relay: CoreRelayProtocolOptions,
        val version: String = "2",
    ) : EngineDO()

    data class SessionProposalEvent(
        val proposal: SessionProposal,
        val context: VerifyContext
    ) : EngineDO(), EngineEvent

    data class SessionRequestEvent(
        val request: SessionRequest,
        val context: VerifyContext
    ) : EngineDO(), EngineEvent

    data class SessionProposal(
        val pairingTopic: String,
        val name: String,
        val description: String,
        val url: String,
        val icons: List<URI>,
        val redirect: String,
        val requiredNamespaces: Map<String, Namespace.Proposal>,
        val optionalNamespaces: Map<String, Namespace.Proposal>,
        val properties: Map<String, String>?,
        val proposerPublicKey: String,
        val relayProtocol: String,
        val relayData: String?,
    ) : EngineDO(), EngineEvent

    data class VerifyContext(
        val id: Long,
        val origin: String,
        val validation: Validation,
        val verifyUrl: String
    ) : EngineDO()

    sealed class Namespace : EngineDO() {

        //Required and Optional
        data class Proposal(
            val chains: List<String>? = null,
            val methods: List<String>,
            val events: List<String>
        ) : Namespace()

        data class Session(
            val chains: List<String>? = null,
            val accounts: List<String>,
            val methods: List<String>,
            val events: List<String>
        ) : Namespace()
    }

    data class SessionRequest(
        val topic: String,
        val chainId: String?,
        val peerAppMetaData: AppMetaData?,
        val request: JSONRPCRequest,
    ) : EngineDO(), EngineEvent {

        data class JSONRPCRequest(
            val id: Long,
            val method: String,
            val params: String,
        ) : EngineDO()
    }

    data class SessionPayloadResponse(
        val topic: String,
        val chainId: String?,
        val method: String,
        val result: JsonRpcResponse,
    ) : EngineDO(), EngineEvent

    data class SessionDelete(
        val topic: String,
        val reason: String,
    ) : EngineDO(), EngineEvent

    data class SessionEvent(
        val topic: String,
        val name: String,
        val data: String,
        val chainId: String?,
    ) : EngineDO(), EngineEvent

    sealed class SettledSessionResponse : EngineDO(), EngineEvent {
        data class Result(val settledSession: Session) : SettledSessionResponse()
        data class Error(val errorMessage: String) : SettledSessionResponse()
    }

    sealed class SessionUpdateNamespacesResponse : EngineDO(), EngineEvent {
        data class Result(val topic: Topic, val namespaces: Map<String, Namespace.Session>) : SessionUpdateNamespacesResponse()
        data class Error(val errorMessage: String) : SessionUpdateNamespacesResponse()
    }

    data class SessionRejected(
        val topic: String,
        val reason: String,
    ) : EngineDO(), EngineEvent

    data class SessionApproved(
        val topic: String,
        val peerAppMetaData: AppMetaData?,
        val accounts: List<String>,
        val namespaces: Map<String, Namespace.Session>,
    ) : EngineDO(), EngineEvent

    data class PairingSettle(val topic: Topic, val appMetaData: AppMetaData?) : EngineDO(), EngineEvent

    data class SessionUpdateNamespaces(val topic: Topic, val namespaces: Map<String, Namespace.Session>) : EngineDO(), EngineEvent

    data class SessionExtend(
        override val topic: Topic,
        override val expiry: Expiry,
        val pairingTopic: String,
        val namespaces: Map<String, Namespace.Session>,
        val peerAppMetaData: AppMetaData?,
    ) : EngineDO(), Sequence, EngineEvent

    data class Session(
        override val topic: Topic,
        override val expiry: Expiry,
        val pairingTopic: String,
        val namespaces: Map<String, Namespace.Session>,
        val peerAppMetaData: AppMetaData?,
    ) : EngineDO(), Sequence, EngineEvent

    data class Event(
        val name: String,
        val data: String,
        val chainId: String,
    ) : EngineDO()

    sealed class JsonRpcResponse : EngineDO() {
        abstract val id: Long

        @JsonClass(generateAdapter = true)
        data class JsonRpcResult(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val result: String,
        ) : JsonRpcResponse()

        @JsonClass(generateAdapter = true)
        data class JsonRpcError(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val error: Error,
        ) : JsonRpcResponse()

        data class Error(
            val code: Int,
            val message: String,
        )
    }

    data class Request(
        val topic: String,
        val method: String,
        val params: String,
        val chainId: String,
        val expiry: Expiry? = null
    ) : EngineDO()
}