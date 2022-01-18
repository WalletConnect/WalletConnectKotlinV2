package com.walletconnect.walletconnectv2.common.model.vo.sequence

import com.walletconnect.walletconnectv2.common.model.type.Sequence
import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.common.model.vo.PublicKey
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingParticipantVO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import org.json.JSONObject


internal data class PendingPairingVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    override val status: SequenceStatus,
    val selfParticipant: PublicKey,
    val proposalUri: String,
) : Sequence

internal data class SettledPairingVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    override val status: SequenceStatus,
    val selfParticipantVO: PublicKey,
    val peerParticipant: PublicKey,
    val relay: JSONObject = JSONObject(),
    val controllerKey: PublicKey
) : Sequence