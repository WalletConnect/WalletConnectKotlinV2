package com.walletconnect.auth.common.model

import com.walletconnect.android.internal.common.model.params.Cacao

internal sealed class Response {
    abstract val id: Long

    data class Result(override val id: Long, val cacao: Cacao) : Response()
    data class Error(override val id: Long, val code: Int, val message: String) : Response()
}
