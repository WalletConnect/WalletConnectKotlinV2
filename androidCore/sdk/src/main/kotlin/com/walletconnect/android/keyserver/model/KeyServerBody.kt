package com.walletconnect.android.keyserver.model

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.signing.cacao.Cacao

sealed class KeyServerBody {

    @JsonClass(generateAdapter = true)
    data class UnregisterIdentity(val idAuth: String) : KeyServerBody()

    @JsonClass(generateAdapter = true)
    data class RegisterIdentity(val cacao: Cacao) : KeyServerBody()

    @JsonClass(generateAdapter = true)
    data class RegisterInvite(val idAuth: String) : KeyServerBody()
    @JsonClass(generateAdapter = true)
    data class UnregisterInvite(val idAuth: String) : KeyServerBody()
}