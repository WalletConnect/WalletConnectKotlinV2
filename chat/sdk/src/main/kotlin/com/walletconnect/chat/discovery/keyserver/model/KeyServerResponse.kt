@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.model

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.cacao.Cacao

internal sealed class KeyServerResponse {

    @JsonClass(generateAdapter = true)
    data class ResolveInvite(val inviteKey: String) : KeyServerResponse()

    @JsonClass(generateAdapter = true)
    data class ResolveIdentity(val cacao: Cacao) : KeyServerResponse()
}