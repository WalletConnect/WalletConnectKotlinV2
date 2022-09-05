@file:JvmSynthetic

package com.walletconnect.auth.common.model

internal sealed class AuthResponse {
    data class Result(val cacao: Cacao) : AuthResponse()
    data class Error(val code: Int, val message: String) : AuthResponse()
}