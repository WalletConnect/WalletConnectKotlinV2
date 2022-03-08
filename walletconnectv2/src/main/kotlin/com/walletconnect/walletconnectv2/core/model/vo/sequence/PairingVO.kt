package com.walletconnect.walletconnectv2.core.model.vo.sequence

import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.AppMetaDataVO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.util.Empty

internal data class PairingVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    override val status: SequenceStatus,
    val selfParticipant: PublicKey,
    val peerParticipant: PublicKey? = null,
    val controllerKey: PublicKey? = null,
    val uri: String,
    val relayProtocol: String,
    val relayData: String?,
    val permissions: List<String>? = null,
    val appMetaDataVO: AppMetaDataVO? = null,
    val outcomeTopic: TopicVO = TopicVO(String.Empty)
) : Sequence {
    val isPeerController: Boolean = peerParticipant?.keyAsHex == controllerKey?.keyAsHex
}