package com.walletconnect.walletconnectv2.engine.model

import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.type.SequenceLifecycle
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.proposal.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import java.net.URI

internal sealed class EngineDO {

    internal class WalletConnectUri(
        val topic: TopicVO,
        val publicKey: PublicKey,
        val isController: Boolean,
        val relay: RelayProtocolOptionsVO,
        val version: String = "2"
    ) : EngineDO()

    internal data class SessionProposal(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<URI>,
        val chains: List<String>,
        val methods: List<String>,
        val types: List<String>? = null,
        val topic: String,
        val publicKey: String,
        val isController: Boolean = false,
        val ttl: Long,
        val accounts: List<String>,
        val relayProtocol: String
    ) : EngineDO(), SequenceLifecycle

    internal data class SessionRequest(
        val topic: String,
        val chainId: String?,
        val request: JSONRPCRequest
    ) : EngineDO(), SequenceLifecycle {

        internal data class JSONRPCRequest(
            val id: Long,
            val method: String,
            val params: String
        ) : EngineDO()
    }

    data class SessionPayloadResponse(
        val topic: String,
        val chainId: String?,
        val result: JsonRpcResponse
    ) : EngineDO(), SequenceLifecycle

    internal data class SessionDelete(
        val topic: String,
        val reason: String
    ) : EngineDO(), SequenceLifecycle

    internal data class DeletedPairing(
        val topic: String,
        val reason: String
    ) : EngineDO(), SequenceLifecycle

    internal data class SessionNotification(
        val topic: String,
        val type: String,
        val data: String
    ) : EngineDO(), SequenceLifecycle

    internal data class SettledPairing(
        val topic: TopicVO,
        val appMetaData: AppMetaData?
    ) : EngineDO(), SequenceLifecycle

    internal data class SessionRejected(
        val topic: String,
        val reason: String
    ) : EngineDO(), SequenceLifecycle

    internal data class SessionApproved(
        val topic: String,
        val peerAppMetaData: AppMetaData?,
        val permissions: SessionPermissions,
        val accounts: List<String>
    ) : EngineDO(), SequenceLifecycle

    internal data class PairingUpdate(val topic: TopicVO, val metaData: AppMetaData) : EngineDO(), SequenceLifecycle
    internal data class SessionUpdate(val topic: TopicVO, val accounts: List<String>) : EngineDO(), SequenceLifecycle
    internal data class SessionUpgrade(val topic: TopicVO, val chains: List<String>, val methods: List<String>) : EngineDO(),
        SequenceLifecycle

    internal object Default : EngineDO(), SequenceLifecycle

    internal data class SettledSession(
        override val topic: TopicVO,
        override val expiry: ExpiryVO,
        override val status: SequenceStatus,
        val accounts: List<String>,
        val peerAppMetaData: AppMetaData?,
        val permissions: Permissions
    ) : EngineDO(), Sequence, SequenceLifecycle {

        internal data class Permissions(
            val blockchain: Blockchain,
            val jsonRpc: JsonRpc,
            val notifications: Notifications
        ) {
            internal data class Blockchain(val chains: List<String>)

            internal data class JsonRpc(val methods: List<String>)

            internal data class Notifications(val types: List<String>?)
        }
    }

    internal data class Notification(
        val type: String,
        val data: String
    ) : EngineDO()

    internal data class SessionState(val accounts: List<String>) : EngineDO()

    internal data class SessionPermissions(
        val blockchain: Blockchain, val jsonRpc: JsonRpc, val notification: Notifications? = null
    ) : EngineDO()

    internal data class Blockchain(val chains: List<String>) : EngineDO()

    internal data class JsonRpc(val methods: List<String>) : EngineDO()

    data class Notifications(val types: List<String>)

    internal data class AppMetaData(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<String>
    ) : EngineDO()

    internal sealed class JsonRpcResponse : EngineDO() {
        abstract val id: Long

        @JsonClass(generateAdapter = true)
        internal data class JsonRpcResult(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val result: String
        ) : JsonRpcResponse()

        @JsonClass(generateAdapter = true)
        internal data class JsonRpcError(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val error: Error
        ) : JsonRpcResponse()

        internal data class Error(
            val code: Int,
            val message: String,
        )
    }

    internal data class Request(val topic: String, val method: String, val params: String, val chainId: String?) : EngineDO()
}