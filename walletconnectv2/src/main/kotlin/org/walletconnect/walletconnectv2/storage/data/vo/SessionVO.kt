package org.walletconnect.walletconnectv2.storage.data.vo

import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.Ttl
import org.walletconnect.walletconnectv2.storage.SequenceStatus

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