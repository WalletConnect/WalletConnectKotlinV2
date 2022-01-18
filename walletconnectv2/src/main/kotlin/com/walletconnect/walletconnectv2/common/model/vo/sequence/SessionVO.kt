package com.walletconnect.walletconnectv2.common.model.vo.sequence

import com.walletconnect.walletconnectv2.common.model.type.Sequence
import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.common.model.vo.PublicKey
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.model.vo.TtlVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.AppMetaDataVO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus

internal data class PendingSessionVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    override val status: SequenceStatus,
    val selfParticipant: PublicKey,
    val chains: List<String>,
    val methods: List<String>,
    val types: List<String>,
    val ttl: TtlVO
) : Sequence

internal data class SettledSessionVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    override val status: SequenceStatus,
    val selfParticipant: PublicKey,
    val peerParticipant: PublicKey,
    val controllerKey: PublicKey,
    val chains: List<String>,
    val methods: List<String>,
    val types: List<String>,
    val ttl: TtlVO,
    val accounts: List<String>,
    val appMetaData: AppMetaDataVO? = null
) : Sequence
