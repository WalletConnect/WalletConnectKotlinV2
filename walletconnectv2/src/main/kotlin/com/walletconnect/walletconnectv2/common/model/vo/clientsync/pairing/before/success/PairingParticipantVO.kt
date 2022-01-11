package com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PairingParticipantVO(val publicKey: String)