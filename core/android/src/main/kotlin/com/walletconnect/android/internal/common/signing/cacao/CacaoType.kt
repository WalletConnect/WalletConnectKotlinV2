package com.walletconnect.android.internal.common.signing.cacao

enum class CacaoType(val header: String) {
    EIP4361("eip4361"),
    CAIP222("caip222");

    fun toHeader(): Cacao.Header = Cacao.Header(this.header)
}