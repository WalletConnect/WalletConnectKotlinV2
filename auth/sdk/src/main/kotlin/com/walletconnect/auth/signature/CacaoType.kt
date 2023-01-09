package com.walletconnect.auth.signature

import com.walletconnect.android.internal.common.model.params.Cacao

enum class CacaoType(val header: String) {
    EIP4361("eip4361");

    internal fun toHeader(): Cacao.Header = Cacao.Header(this.header)
}