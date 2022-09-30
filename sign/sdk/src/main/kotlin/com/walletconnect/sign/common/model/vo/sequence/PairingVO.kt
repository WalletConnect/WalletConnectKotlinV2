@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.sequence

import com.walletconnect.android.impl.common.model.MetaData
import com.walletconnect.android.impl.utils.ACTIVE_PAIRING
import com.walletconnect.android.impl.utils.INACTIVE_PAIRING
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.android.common.model.Sequence
import com.walletconnect.android.common.model.RelayProtocolOptions
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toAbsoluteString

internal data class PairingVO(
    override val topic: Topic,
    override val expiry: com.walletconnect.android.common.model.Expiry,
    val relayProtocol: String,
    val relayData: String?,
    val uri: String,
    val isActive: Boolean,
    val peerMetaData: MetaData? = null
) : Sequence {

    constructor(topic: Topic, relay: RelayProtocolOptions, uri: String) : this(
        topic,
        com.walletconnect.android.common.model.Expiry(INACTIVE_PAIRING),
        relay.protocol,
        relay.data,
        uri,
        false
    )

    constructor(uri: EngineDO.WalletConnectUri) : this(
        uri.topic,
        com.walletconnect.android.common.model.Expiry(ACTIVE_PAIRING),
        uri.relay.protocol,
        uri.relay.data,
        uri.toAbsoluteString(),
        true
    )
}