@file:JvmSynthetic

package com.walletconnect.auth.common.model

import com.walletconnect.android_core.common.model.Expiry
import com.walletconnect.android_core.common.model.MetaData
import com.walletconnect.android_core.common.model.RelayProtocolOptions
import com.walletconnect.android_core.common.model.type.Sequence
import com.walletconnect.android_core.utils.ACTIVE_PAIRING
import com.walletconnect.android_core.utils.INACTIVE_PAIRING
import com.walletconnect.auth.engine.mapper.toAbsoluteString
import com.walletconnect.foundation.common.model.Topic

internal data class Pairing(
    override val topic: Topic,
    override val expiry: Expiry,
    val peerMetaData: MetaData? = null,
    val relayProtocol: String,
    val relayData: String?,
    val uri: String,
    val isActive: Boolean,
) : Sequence {

    constructor(topic: Topic, relay: RelayProtocolOptions, uri: String) : this(
        topic = topic,
        expiry = Expiry(INACTIVE_PAIRING),
        relayProtocol = relay.protocol,
        relayData = relay.data,
        uri = uri,
        isActive = false
    )

    constructor(uri: WalletConnectUri) : this(
        topic = uri.topic,
        expiry = Expiry(ACTIVE_PAIRING),
        relayProtocol = uri.relay.protocol,
        relayData = uri.relay.data,
        uri = uri.toAbsoluteString(),
        isActive = true
    )
}
