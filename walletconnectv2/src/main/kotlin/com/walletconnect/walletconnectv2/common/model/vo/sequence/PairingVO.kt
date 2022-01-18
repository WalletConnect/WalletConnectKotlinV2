package com.walletconnect.walletconnectv2.common.model.vo.sequence

import com.walletconnect.walletconnectv2.common.model.type.Sequence
import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.common.model.vo.PublicKey
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingParticipantVO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.util.Empty
import org.json.JSONObject

internal data class PairingVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    override val status: SequenceStatus,
    val selfParticipant: PublicKey,
    val peerParticipant: PublicKey? = null,
    val controllerKey: PublicKey? = null,
    val proposalUri: String,
    val relayProtocol: String,
    val permissions: List<String>? = null
) : Sequence