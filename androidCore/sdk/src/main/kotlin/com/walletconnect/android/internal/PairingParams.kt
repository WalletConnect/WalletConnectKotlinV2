package com.walletconnect.android.internal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.common.model.ClientParams
import com.walletconnect.android.common.model.RelayProtocolOptions
import com.walletconnect.android.common.model.SessionProposer
import com.walletconnect.android.utils.DefaultId

sealed class PairingParams : ClientParams {

    @JsonClass(generateAdapter = true)
    class DeleteParams(
        @Json(name = "code")
        val code: Int = Int.DefaultId,
        @Json(name = "message")
        val message: String,
    ) : PairingParams()

    @Suppress("CanSealedSubClassBeObject")
    class PingParams : PairingParams()
}