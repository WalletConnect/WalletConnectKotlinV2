@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.model

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.cacao.Cacao

internal sealed class KeyServerDTO {
    @JsonClass(generateAdapter = true)
    data class ResolveInviteResponse(val inviteKey: String): KeyServerDTO()

    @JsonClass(generateAdapter = true)
    data class RegisterInviteBody(val idAuth: String): KeyServerDTO()

    @JsonClass(generateAdapter = true)
    data class ResolveIdentityResponse(val cacao: Cacao): KeyServerDTO()

    @JsonClass(generateAdapter = true)
    data class RegisterIdentityBody(val cacao: Cacao): KeyServerDTO()
}