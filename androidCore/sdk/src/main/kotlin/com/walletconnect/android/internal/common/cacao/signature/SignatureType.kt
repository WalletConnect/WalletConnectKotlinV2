package com.walletconnect.android.internal.common.cacao.signature


// Note: Szymon - Only added to have backwards compatibility. This ties SignatureTypes from android-core to sdks specific implementations. When we decide to remove deprecated sdk specific
// implementations this could be removed as well.
interface ISignatureType {
    val header: String
}

enum class SignatureType(override val header: String) : ISignatureType {
    EIP191("eip191"), EIP1271("eip1271");
}