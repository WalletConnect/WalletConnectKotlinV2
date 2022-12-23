@file:JvmSynthetic

package com.walletconnect.auth.common.model

import com.walletconnect.android.internal.common.model.params.Cacao

internal sealed class AuthResponse {
    data class Result(val cacao: Cacao) : AuthResponse()
    data class Error(val code: Int, val message: String) : AuthResponse()
}