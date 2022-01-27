package com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.before.success

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PairingParticipantVO(val publicKey: String)