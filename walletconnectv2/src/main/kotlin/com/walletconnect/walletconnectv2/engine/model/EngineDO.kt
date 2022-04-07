package com.walletconnect.walletconnectv2.engine.model

import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.type.SequenceLifecycle
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import java.net.URI

internal sealed class EngineDO {

    internal sealed class ProposedSequence {
        class Pairing(val uri: String) : ProposedSequence()
        object Session : ProposedSequence()
    }

    internal class WalletConnectUri(
        val topic: TopicVO,
        val symKey: SecretKey,
        val relay: RelayProtocolOptionsVO,
        val version: String = "2",
    ) : EngineDO()

    internal data class SessionProposal(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<URI>,
        val chains: List<String>,
        val methods: List<String>,
        val types: List<String>? = null,
        val proposerPublicKey: String,
        val accounts: List<String>,
        val relayProtocol: String,
        val relayData: String?,
    ) : EngineDO(), SequenceLifecycle

    internal data class SessionRequest(
        val topic: String,
        val chainId: String?,
        val request: JSONRPCRequest,
    ) : EngineDO(), SequenceLifecycle {

        internal data class JSONRPCRequest(
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
    ) : EngineDO(), SequenceLifecycle

    internal data class SessionDelete(
        val topic: String,
        val reason: String,
    ) : EngineDO(), SequenceLifecycle

    internal data class DeletedPairing(
        val topic: String,
        val reason: String,
    ) : EngineDO(), SequenceLifecycle

    internal data class SessionEvent(
        val topic: String,
        val name: String,
        val data: String,
        val chainId: String?,
    ) : EngineDO(), SequenceLifecycle

    sealed class SettledSessionResponse : EngineDO(), SequenceLifecycle {
        data class Result(val settledSession: Session) : SettledSessionResponse()
        data class Error(val errorMessage: String) : SettledSessionResponse()
    }

    sealed class SessionUpgradeResponse : EngineDO(), SequenceLifecycle {
        data class Result(val topic: TopicVO, val methods: List<String>, val types: List<String>?) : SessionUpgradeResponse()
        data class Error(val errorMessage: String) : SessionUpgradeResponse()
    }

    sealed class SessionUpdateResponse : EngineDO(), SequenceLifecycle {
        data class Result(val topic: TopicVO, val accounts: List<String>) : SessionUpdateResponse()
        data class Error(val errorMessage: String) : SessionUpdateResponse()
    }

    internal data class SessionRejected(
        val topic: String,
        val reason: String,
    ) : EngineDO(), SequenceLifecycle

    internal data class SessionApproved(
        val topic: String,
        val peerAppMetaData: AppMetaData?,
        val permissions: SessionPermissions,
        val accounts: List<String>,
    ) : EngineDO(), SequenceLifecycle

    internal data class PairingSettle(val topic: TopicVO, val metaData: AppMetaData?) : EngineDO(), SequenceLifecycle
    internal data class SessionUpdate(val topic: TopicVO, val accounts: List<String>) : EngineDO(), SequenceLifecycle
    internal data class SessionUpgrade(val topic: TopicVO, val types: List<String>, val methods: List<String>) : EngineDO(),
        SequenceLifecycle

    internal data class SessionExtend(
        override val topic: TopicVO,
        override val expiry: ExpiryVO,
        val accounts: List<String>,
        val peerAppMetaData: AppMetaData?,
        val permissions: SessionPermissions,
        val blockchain: Blockchain,
    ) : EngineDO(), Sequence, SequenceLifecycle

    internal data class Session(
        override val topic: TopicVO,
        override val expiry: ExpiryVO,
        val accounts: List<String>,
        val peerAppMetaData: AppMetaData?,
        val permissions: SessionPermissions,
        val blockchain: Blockchain,
    ) : EngineDO(), Sequence, SequenceLifecycle

    internal data class SessionPermissions(
        val jsonRpc: JsonRpc,
        val events: Events? = null,
    ) : EngineDO() {
        internal data class JsonRpc(val methods: List<String>) : EngineDO()
        data class Events(val names: List<String>) : EngineDO()
    }

    internal data class Event(
        val name: String,
        val data: String,
        val chainId: String?,
    ) : EngineDO()

    internal data class SessionState(val accounts: List<String>) : EngineDO()
    internal data class Blockchain(val chains: List<String>) : EngineDO()
    internal data class AppMetaData(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<String>,
    ) : EngineDO()

    internal sealed class JsonRpcResponse : EngineDO() {
        abstract val id: Long

        @JsonClass(generateAdapter = true)
        internal data class JsonRpcResult(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val result: String,
        ) : JsonRpcResponse()

        @JsonClass(generateAdapter = true)
        internal data class JsonRpcError(
            override val id: Long,
            val jsonrpc: String = "2.0",
            val error: Error,
        ) : JsonRpcResponse()

        internal data class Error(
            val code: Int,
            val message: String,
        )
    }

    internal data class Request(val topic: String, val method: String, val params: String, val chainId: String?) : EngineDO()
}