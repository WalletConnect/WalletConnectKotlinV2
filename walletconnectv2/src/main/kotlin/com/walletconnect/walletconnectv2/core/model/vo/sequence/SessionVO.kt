package com.walletconnect.walletconnectv2.core.model.vo.sequence

import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.type.enums.ControllerType
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.TtlVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.proposal.AppMetaDataVO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.util.Empty

internal data class SessionVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    override val status: SequenceStatus,
    val selfParticipant: PublicKey,
    val peerParticipant: PublicKey? = null,
    val controllerKey: PublicKey? = null,
    val chains: List<String>,
    val methods: List<String>,
    val types: List<String>?,
    val ttl: TtlVO,
    val accounts: List<String> = emptyList(),
    val metaData: AppMetaDataVO? = null,
    val controllerType: ControllerType,
    val relayProtocol: String,
    val outcomeTopic: TopicVO = TopicVO(String.Empty)
) : Sequence {
    val isPeerController: Boolean = peerParticipant?.keyAsHex == controllerKey?.keyAsHex
}
