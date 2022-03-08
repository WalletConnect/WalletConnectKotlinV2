package com.walletconnect.walletconnectv2.core.model.vo.sequence

import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.AppMetaDataVO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.util.Empty

internal data class SessionVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    override val status: SequenceStatus,
    val relayProtocol: String,
    val relayData: String?,
    val controllerKey: PublicKey? = null,
    val selfParticipant: PublicKey,
    val selfMetaData: AppMetaDataVO? = null,
    val peerParticipant: PublicKey? = null,
    val peerMetaData: AppMetaDataVO? = null,
    val accounts: List<String> = emptyList(),
    val chains: List<String>,
    val methods: List<String>,
    val types: List<String>?,
    val outcomeTopic: TopicVO = TopicVO(String.Empty)
) : Sequence {
    val isPeerController: Boolean = peerParticipant?.keyAsHex == controllerKey?.keyAsHex
    val isSelfController: Boolean = selfParticipant.keyAsHex == controllerKey?.keyAsHex
}