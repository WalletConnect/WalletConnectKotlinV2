@file:JvmSynthetic

package com.walletconnect.auth.common.model

import com.walletconnect.android.common.model.Expiry
import com.walletconnect.android.impl.common.model.MetaData
import com.walletconnect.android.impl.common.model.RelayProtocolOptions
import com.walletconnect.android.impl.utils.ACTIVE_PAIRING
import com.walletconnect.android.impl.utils.INACTIVE_PAIRING
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.android.impl.common.model.type.Sequence
import com.walletconnect.auth.engine.mapper.toAbsoluteString

internal data class Pairing(
    override val topic: Topic,
    override val expiry: com.walletconnect.android.common.model.Expiry,
    val peerMetaData: MetaData? = null,
    val relayProtocol: String,
    val relayData: String?,
    val uri: String,
    val isActive: Boolean,
) : Sequence {

    constructor(topic: Topic, relay: RelayProtocolOptions, uri: String) : this(
        topic = topic,
        expiry = com.walletconnect.android.common.model.Expiry(INACTIVE_PAIRING),
        relayProtocol = relay.protocol,
        relayData = relay.data,
        uri = uri,
        isActive = false
    )

    constructor(uri: WalletConnectUri) : this(
        topic = uri.topic,
        expiry = com.walletconnect.android.common.model.Expiry(ACTIVE_PAIRING),
        relayProtocol = uri.relay.protocol,
        relayData = uri.relay.data,
        uri = uri.toAbsoluteString(),
        isActive = true
    )
}
