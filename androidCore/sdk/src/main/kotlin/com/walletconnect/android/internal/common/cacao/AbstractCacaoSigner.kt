@file:JvmSynthetic

package com.walletconnect.android.internal.common.cacao

import com.walletconnect.android.internal.common.cacao.eip191.EIP191Signer
import com.walletconnect.android.internal.common.cacao.signature.ISignatureType
import com.walletconnect.android.internal.common.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.cacao.signature.toCacaoSignature

interface ISignature {
    val t: String
    val s: String
    val m: String?
}

abstract class AbstractCacaoSigner<T : ISignature> {
    abstract fun sign(message: ByteArray, privateKey: ByteArray, type: ISignatureType): T
    abstract fun sign(message: String, privateKey: ByteArray, type: ISignatureType): T
    protected abstract fun Cacao.Signature.map(): T
}

abstract class SDKCacaoSigner<T : ISignature> : AbstractCacaoSigner<T>() {
    @Suppress("UNCHECKED_CAST") // Added to dismiss confusion. Cast to `T` always succeeds as Cacao.Signature implements ISignature.
    override fun sign(message: ByteArray, privateKey: ByteArray, type: ISignatureType): T = when (type.header) {
        SignatureType.EIP191.header, SignatureType.EIP1271.header -> Cacao.Signature(type.header, EIP191Signer.sign(message, privateKey).toCacaoSignature()) as T
        else -> throw Throwable("SignatureType not recognized")
    }

    override fun sign(message: String, privateKey: ByteArray, type: ISignatureType): T = sign(message.toByteArray(), privateKey, type)

    abstract override fun Cacao.Signature.map(): T
}

object CoreCacaoSigner: SDKCacaoSigner<Cacao.Signature>() {
    override fun Cacao.Signature.map(): Cacao.Signature = this
}

