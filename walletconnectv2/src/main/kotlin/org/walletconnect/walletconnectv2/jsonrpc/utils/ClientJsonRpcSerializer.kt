package org.walletconnect.walletconnectv2.jsonrpc.utils

import org.walletconnect.walletconnectv2.ClientParams
import org.walletconnect.walletconnectv2.clientsync.ClientSyncJsonRpc
import org.walletconnect.walletconnectv2.clientsync.pairing.after.PostSettlementPairing
import org.walletconnect.walletconnectv2.clientsync.pairing.before.PreSettlementPairing
import org.walletconnect.walletconnectv2.clientsync.session.after.PostSettlementSession
import org.walletconnect.walletconnectv2.clientsync.session.before.PreSettlementSession
import org.walletconnect.walletconnectv2.jsonrpc.model.JsonRpcResponse
import org.walletconnect.walletconnectv2.serailising.tryDeserialize
import org.walletconnect.walletconnectv2.serailising.trySerialize
import org.walletconnect.walletconnectv2.util.empty

object ClientJsonRpcSerializer {

    fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_PAIRING_APPROVE -> tryDeserialize<PreSettlementPairing.Approve>(json)?.params
            JsonRpcMethod.WC_PAIRING_REJECT -> tryDeserialize<PreSettlementPairing.Reject>(json)?.params
            JsonRpcMethod.WC_PAIRING_PAYLOAD -> tryDeserialize<PostSettlementPairing.PairingPayload>(json)?.params
            JsonRpcMethod.WC_PAIRING_UPDATE -> tryDeserialize<PostSettlementPairing.PairingUpdate>(json)?.params
            JsonRpcMethod.WC_PAIRING_PING -> tryDeserialize<PostSettlementPairing.PairingPing>(json)?.params
            JsonRpcMethod.WC_PAIRING_NOTIFICATION -> tryDeserialize<PostSettlementPairing.PairingPing>(json)?.params
            JsonRpcMethod.WC_SESSION_APPROVE -> tryDeserialize<PreSettlementSession.Approve>(json)?.params
            JsonRpcMethod.WC_SESSION_REJECT -> tryDeserialize<PreSettlementSession.Reject>(json)?.params
            JsonRpcMethod.WC_SESSION_PROPOSE -> tryDeserialize<PreSettlementSession.Proposal>(json)?.params
            JsonRpcMethod.WC_SESSION_PAYLOAD -> tryDeserialize<PostSettlementSession.SessionPayload>(json)?.params
            JsonRpcMethod.WC_SESSION_DELETE -> tryDeserialize<PostSettlementSession.SessionDelete>(json)?.params
            JsonRpcMethod.WC_SESSION_UPDATE -> tryDeserialize<PostSettlementSession.SessionUpdate>(json)?.params
            JsonRpcMethod.WC_SESSION_UPGRADE -> tryDeserialize<PostSettlementSession.SessionUpgrade>(json)?.params
            JsonRpcMethod.WC_SESSION_PING -> tryDeserialize<PostSettlementSession.SessionPing>(json)?.params
            JsonRpcMethod.WC_SESSION_NOTIFICATION -> tryDeserialize<PostSettlementSession.SessionNotification>(json)?.params
            else -> null
        }

    fun serialize(payload: ClientSyncJsonRpc): String =
        when (payload) {
            is PreSettlementPairing.Approve -> trySerialize(payload)
            is PreSettlementPairing.Reject -> trySerialize(payload)
            is PostSettlementPairing.PairingPayload -> trySerialize(payload)
            is PostSettlementPairing.PairingNotification -> trySerialize(payload)
            is PostSettlementPairing.PairingPing -> trySerialize(payload)
            is PostSettlementPairing.PairingUpdate -> trySerialize(payload)
            is PreSettlementSession.Approve -> trySerialize(payload)
            is PreSettlementSession.Reject -> trySerialize(payload)
            is PreSettlementSession.Proposal -> trySerialize(payload)
            is PostSettlementSession.SessionNotification -> trySerialize(payload)
            is PostSettlementSession.SessionPing -> trySerialize(payload)
            is PostSettlementSession.SessionUpdate -> trySerialize(payload)
            is PostSettlementSession.SessionUpgrade -> trySerialize(payload)
            is PostSettlementSession.SessionPayload -> trySerialize(payload)
            is PostSettlementSession.SessionDelete -> trySerialize(payload)
            is JsonRpcResponse -> trySerialize(payload)
            else -> String.empty
        }
}