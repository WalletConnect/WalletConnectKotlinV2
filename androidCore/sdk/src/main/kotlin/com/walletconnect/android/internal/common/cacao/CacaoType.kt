package com.walletconnect.android.internal.common.cacao

enum class CacaoType(val header: String) {
    EIP4361("eip4361");

    fun toHeader(): Cacao.Header = Cacao.Header(this.header)
}