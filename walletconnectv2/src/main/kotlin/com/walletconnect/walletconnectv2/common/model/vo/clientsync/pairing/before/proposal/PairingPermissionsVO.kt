package com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.proposal

import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingParticipantVO

@JsonClass(generateAdapter = true)
internal data class PairingPermissionsVO(
    val controller: PairingParticipantVO
)
