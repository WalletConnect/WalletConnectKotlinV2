package com.walletconnect.android.internal.common.model

import com.walletconnect.android.internal.common.model.type.Sequence
import com.walletconnect.android.internal.utils.CURRENT_TIME_IN_SECONDS
import com.walletconnect.android.pairing.model.INACTIVE_PAIRING
import com.walletconnect.foundation.common.model.Topic

data class Pairing(
    override val topic: Topic,
    override val expiry: Expiry,
    val peerAppMetaData: AppMetaData? = null,
    val relayProtocol: String,
    val relayData: String?,
    val uri: String,
    val registeredMethods: String
) : Sequence {
    val isActive: Boolean
        get() = (expiry.seconds > CURRENT_TIME_IN_SECONDS)

    constructor(topic: Topic, relay: RelayProtocolOptions, symmetricKey: SymmetricKey, registeredMethods: String) : this(
        topic = topic,
        expiry = Expiry(INACTIVE_PAIRING),
        relayProtocol = relay.protocol,
        relayData = relay.data,
        uri = WalletConnectUri(topic, symmetricKey, relay).toAbsoluteString(),
        registeredMethods = registeredMethods
    )

    constructor(uri: WalletConnectUri, registeredMethods: String) : this(
        topic = uri.topic,
        expiry = Expiry(INACTIVE_PAIRING),
        relayProtocol = uri.relay.protocol,
        relayData = uri.relay.data,
        uri = uri.toAbsoluteString(),
        registeredMethods = registeredMethods
    )
}