@file:JvmSynthetic

package com.walletconnect.auth.common.model

import com.walletconnect.auth.client.Auth

internal sealed class Respond {
    abstract val id: Long

    data class Result(override val id: Long, val signature: Auth.Model.Cacao.Signature, val iss: String) : Respond()
    data class Error(override val id: Long, val code: Int, val message: String) : Respond()
}