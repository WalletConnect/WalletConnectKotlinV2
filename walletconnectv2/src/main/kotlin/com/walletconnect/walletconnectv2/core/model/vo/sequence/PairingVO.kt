package com.walletconnect.walletconnectv2.core.model.vo.sequence

import com.walletconnect.walletconnectv2.core.model.type.ControllerType
import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.proposal.AppMetaDataVO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus

internal data class PairingVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    override val status: SequenceStatus,
    val selfParticipant: PublicKey,
    val peerParticipant: PublicKey? = null,
    val controllerKey: PublicKey? = null,
    val uri: String,
    val relay: String,
    val permissions: List<String>? = null,
    val controllerType: ControllerType,
    val appMetaDataVO: AppMetaDataVO? = null
) : Sequence {
    val isPeerController: Boolean = peerParticipant?.keyAsHex == controllerKey?.keyAsHex
}