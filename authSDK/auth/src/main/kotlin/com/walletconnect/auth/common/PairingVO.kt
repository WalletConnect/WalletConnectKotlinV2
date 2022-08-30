package com.walletconnect.auth.common

import com.walletconnect.android.impl.common.model.Expiry
import com.walletconnect.android.impl.common.model.MetaData
import com.walletconnect.android.impl.common.model.RelayProtocolOptions
import com.walletconnect.android.impl.utils.ACTIVE_PAIRING
import com.walletconnect.android.impl.utils.INACTIVE_PAIRING
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.android.impl.common.model.type.Sequence
import com.walletconnect.auth.engine.model.EngineDO
import com.walletconnect.auth.engine.model.toAbsoluteString

internal data class PairingVO(
    override val topic: Topic,
    override val expiry: Expiry,
    val relayProtocol: String,
    val relayData: String?,
    val uri: String,
    val isActive: Boolean,
    val peerMetaData: MetaData? = null,
) : Sequence {

    constructor(topic: Topic, relay: RelayProtocolOptions, uri: String) : this(
        topic,
        Expiry(INACTIVE_PAIRING),
        relay.protocol,
        relay.data,
        uri,
        false
    )

    constructor(uri: EngineDO.WalletConnectUri) : this(
        uri.topic,
        Expiry(ACTIVE_PAIRING),
        uri.relay.protocol,
        uri.relay.data,
        uri.toAbsoluteString(),
        true
    )
}
