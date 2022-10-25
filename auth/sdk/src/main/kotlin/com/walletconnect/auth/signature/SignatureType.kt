package com.walletconnect.auth.signature


enum class SignatureType(val header: String) {
    EIP191("eip191"), EIP1271("eip1271");
}