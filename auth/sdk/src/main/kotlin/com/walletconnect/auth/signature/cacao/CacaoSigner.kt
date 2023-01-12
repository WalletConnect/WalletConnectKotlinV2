@file:JvmSynthetic

package com.walletconnect.auth.signature.cacao

import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.android.internal.common.cacao.SDKCacaoSigner
import com.walletconnect.android.internal.common.cacao.signature.ISignatureType
import com.walletconnect.auth.client.Auth

/// Note: Szymon - Only added to have backwards compatibility. Newer SDKs should only add CacaoSigner object below.
@Deprecated("Moved to android-core module, as other SDKs also need CACAO.", ReplaceWith("com.walletconnect.android.internal.common.cacao.signature.SignatureType"))
enum class SignatureType(override val header: String) : ISignatureType {
    EIP191("eip191"), EIP1271("eip1271");
}

object CacaoSigner : SDKCacaoSigner<Auth.Model.Cacao.Signature>() {
    @Suppress("CAST_NEVER_SUCCEEDS") // Added to dismiss confusion. Cast to `Cacao.Signature` always succeeds.
    override fun sign(message: ByteArray, privateKey: ByteArray, type: ISignatureType): Auth.Model.Cacao.Signature =
        (super<SDKCacaoSigner>.sign(message, privateKey, type) as Cacao.Signature).map()

    override fun sign(message: String, privateKey: ByteArray, type: ISignatureType): Auth.Model.Cacao.Signature = sign(message.toByteArray(), privateKey, type)

    override fun Cacao.Signature.map(): Auth.Model.Cacao.Signature = Auth.Model.Cacao.Signature(this.t, this.s, this.m)
}

