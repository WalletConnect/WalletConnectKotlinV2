package com.walletconnect.android.keyserver.model

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.signing.cacao.Cacao

sealed class KeyServerResponse {

    @JsonClass(generateAdapter = true)
    data class ResolveInvite(val inviteKey: String) : KeyServerResponse()

    @JsonClass(generateAdapter = true)
    data class ResolveIdentity(val cacao: Cacao) : KeyServerResponse()
}