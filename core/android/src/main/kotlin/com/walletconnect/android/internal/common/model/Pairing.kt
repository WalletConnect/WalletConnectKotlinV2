package com.walletconnect.android.internal.common.model

import com.walletconnect.android.internal.common.model.type.Sequence
import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.android.pairing.model.inactivePairing
import com.walletconnect.foundation.common.model.Topic

data class
Pairing(
    override val topic: Topic,
    override val expiry: Expiry,
    val peerAppMetaData: AppMetaData? = null,
    val relayProtocol: String,
    val relayData: String?,
    val uri: String,
    val isProposalReceived: Boolean = false,
    val methods: String? = null,
) : Sequence {
    val isActive: Boolean
        get() = (expiry.seconds - currentTimeInSeconds) > fiveMinutesInSeconds

    constructor(topic: Topic, relay: RelayProtocolOptions, symmetricKey: SymmetricKey, expiry: Expiry, methods: String?) : this(
        topic = topic,
        expiry = expiry,
        relayProtocol = relay.protocol,
        relayData = relay.data,
        uri = WalletConnectUri(topic, symmetricKey, relay, expiry = expiry, methods = methods).toAbsoluteString(),
        methods = methods,
    )

    constructor(uri: WalletConnectUri) : this(
        topic = uri.topic,
        expiry = uri.expiry ?: Expiry(inactivePairing),
        relayProtocol = uri.relay.protocol,
        relayData = uri.relay.data,
        uri = uri.toAbsoluteString(),
        methods = uri.methods,
    )
}