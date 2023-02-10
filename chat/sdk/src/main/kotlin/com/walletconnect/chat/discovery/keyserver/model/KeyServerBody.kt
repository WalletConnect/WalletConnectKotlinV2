@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.model

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.cacao.Cacao

internal sealed class KeyServerBody {

    @JsonClass(generateAdapter = true)
    data class UnregisterIdentity(val cacao: Cacao) : KeyServerBody()

    @JsonClass(generateAdapter = true)
    data class RegisterIdentity(val cacao: Cacao) : KeyServerBody()

    @JsonClass(generateAdapter = true)
    data class RegisterInvite(val idAuth: String) : KeyServerBody()
    @JsonClass(generateAdapter = true)
    data class UnregisterInvite(val idAuth: String) : KeyServerBody()
}