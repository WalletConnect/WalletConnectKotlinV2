package com.walletconnect.walletconnectv2.storage.sequence.vo

import com.walletconnect.walletconnectv2.common.model.Expiry
import com.walletconnect.walletconnectv2.common.model.Topic
import com.walletconnect.walletconnectv2.common.model.Ttl
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus

data class SessionVO(
    val topic: Topic,
    val chains: List<String>,
    val methods: List<String>,
    val types: List<String>,
    val ttl: Ttl,
    val accounts: List<String>,
    val expiry: Expiry,
    val status: SequenceStatus,
    val appMetaData: AppMetaDataVO?
)