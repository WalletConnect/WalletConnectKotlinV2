package com.walletconnect.android.internal.pairing

import com.walletconnect.android.internal.PairingParams
import com.walletconnect.foundation.common.model.Topic

@JvmSynthetic
internal fun PairingParams.SessionProposeParams.toEngineDO(): PairingDO.SessionProposal =
    PairingDO.SessionProposal(
        name = this.proposer.metadata.name,
        description = this.proposer.metadata.description,
        url = this.proposer.metadata.url,
        icons = this.proposer.metadata.icons.map { URI(it) },
        requiredNamespaces = this.namespaces.toMapOfEngineNamespacesProposal(),
        proposerPublicKey = this.proposer.publicKey,
        relayProtocol = relays.first().protocol,
        relayData = relays.first().data
    )

@JvmSynthetic
internal fun PairingParams.DeleteParams.toEngineDO(topic: Topic): PairingDO.PairingDelete =
    PairingDO.PairingDelete(topic.value, message)