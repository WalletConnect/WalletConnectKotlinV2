package com.walletconnect.auth.signature

import com.walletconnect.auth.common.model.Cacao

enum class CacaoType(val header: String) {
    EIP4361("eip4361");

    internal fun toHeader(): Cacao.Header = Cacao.Header(this.header)
}