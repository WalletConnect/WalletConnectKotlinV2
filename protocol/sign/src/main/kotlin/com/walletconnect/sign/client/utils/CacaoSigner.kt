package com.walletconnect.sign.client.utils

import com.walletconnect.android.cacao.signature.ISignatureType
import com.walletconnect.android.utils.cacao.CacaoSignerInterface
import com.walletconnect.sign.client.Sign

/// Only added to have backwards compatibility. Newer SDKs should only add CacaoSigner object below.
@Deprecated("Moved to android-core module, as other SDKs also need CACAO.", ReplaceWith("com.walletconnect.android.internal.common.cacao.signature.SignatureType"))
enum class SignatureType(override val header: String) : ISignatureType {
    EIP191("eip191"), EIP1271("eip1271");
}

object CacaoSigner : CacaoSignerInterface<Sign.Model.Cacao.Signature>