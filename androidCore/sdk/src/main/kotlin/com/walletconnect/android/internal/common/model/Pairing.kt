package com.walletconnect.android.internal.common.model

import com.walletconnect.android.pairing.model.ACTIVE_PAIRING
import com.walletconnect.android.pairing.model.INACTIVE_PAIRING
import com.walletconnect.foundation.common.model.Topic

data class Pairing(
    override val topic: Topic,
    override val expiry: Expiry,
    val peerAppMetaData: AppMetaData? = null,
    val relayProtocol: String,
    val relayData: String?,
    val uri: String,
    val isActive: Boolean,
    val registeredMethods: String
) : Sequence {

    constructor(topic: Topic, relay: RelayProtocolOptions, symmetricKey: SymmetricKey, registeredMethods: String) : this(
        topic = topic,
        expiry = Expiry(INACTIVE_PAIRING),
        relayProtocol = relay.protocol,
        relayData = relay.data,
        uri = WalletConnectUri(topic, symmetricKey, relay).toAbsoluteString(),
        isActive = false,
        registeredMethods = registeredMethods
    )

    constructor(uri: WalletConnectUri, registeredMethods: String) : this(
        topic = uri.topic,
        expiry = Expiry(ACTIVE_PAIRING),
        relayProtocol = uri.relay.protocol,
        relayData = uri.relay.data,
        uri = uri.toAbsoluteString(),
        isActive = true,
        registeredMethods = registeredMethods
    )
}