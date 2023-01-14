@file:JvmSynthetic
@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.android.cacao

import com.walletconnect.android.cacao.signature.ISignatureType
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.android.internal.common.cacao.eip191.EIP191Signer
import com.walletconnect.android.internal.common.cacao.signature.SignatureInterface
import com.walletconnect.android.internal.common.cacao.signature.toCacaoSignature

interface CacaoSignerInterface<CoreSignature : SignatureInterface>

@Suppress("UNCHECKED_CAST", "unused") // Added to dismiss confusion. Cast to `T` always succeeds as Cacao.Signature implements ISignature.
inline fun <CoreSignature : SignatureInterface, reified SDKSignature : CoreSignature> CacaoSignerInterface<CoreSignature>.sign(
    message: String,
    privateKey: ByteArray,
    type: ISignatureType,
): CoreSignature =
    when (type.header) {
        SignatureType.EIP191.header, SignatureType.EIP1271.header -> {
            (Cacao.Signature(type.header, EIP191Signer.sign(message.toByteArray(), privateKey).toCacaoSignature()) as SignatureInterface).run {
                SDKSignature::class.constructors.first().call(t, s, m)
            }
        }
        else -> throw Throwable("SignatureType not recognized")
    }

