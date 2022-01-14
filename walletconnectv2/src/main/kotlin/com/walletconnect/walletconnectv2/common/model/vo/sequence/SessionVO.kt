package com.walletconnect.walletconnectv2.common.model.vo.sequence

import com.walletconnect.walletconnectv2.common.model.type.Sequence
import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.model.vo.TtlVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.AppMetaDataVO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus

internal data class SessionVO(
    override val topic: TopicVO,
    val chains: List<String>,
    val methods: List<String>,
    val types: List<String>,
    val ttl: TtlVO,
    val accounts: List<String>,
    override val expiry: ExpiryVO,
    override val status: SequenceStatus,
    val appMetaData: AppMetaDataVO?
) : Sequence