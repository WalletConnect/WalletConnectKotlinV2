@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.clientsync.pairing.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.impl.common.model.MetaData

@JsonClass(generateAdapter = true)
internal data class SessionProposerVO(
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "metadata")
    val metadata: MetaData,
)