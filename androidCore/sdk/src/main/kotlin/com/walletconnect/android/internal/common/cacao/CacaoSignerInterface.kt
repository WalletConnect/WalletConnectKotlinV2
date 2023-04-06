@file:JvmSynthetic
@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.android.cacao

import com.walletconnect.android.cacao.signature.ISignatureType
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.android.internal.common.cacao.eip191.EIP191Signer
import com.walletconnect.android.internal.common.cacao.signature.toCacaoSignature
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.kotlinFunction

interface CacaoSignerInterface<CoreSignature : SignatureInterface>

@Suppress("UNCHECKED_CAST", "unused") // Added to dismiss confusion. Cast to `T` always succeeds as Cacao.Signature implements ISignature.
inline fun <CoreSignature : SignatureInterface, reified SDKSignature : CoreSignature> CacaoSignerInterface<CoreSignature>.sign(
    message: String,
    privateKey: ByteArray,
    type: ISignatureType,
): CoreSignature =
    when (type.header) {
        SignatureType.EIP191.header, SignatureType.EIP1271.header -> {
            Cacao.Signature(type.header, EIP191Signer.sign(message.toByteArray(), privateKey).toCacaoSignature()).run {
                SDKSignature::class.constructors.first(KFunction<SDKSignature>::hasCorrectOrderedParametersInConstructor).call(t, s, m)
            }
        }
        else -> throw Throwable("SignatureType not recognized")
    }

fun <T : SignatureInterface> signCacao(clazz: Class<T>, message: String, privateKey: ByteArray, type: ISignatureType): SignatureInterface =
    when (type.header) {
        SignatureType.EIP191.header, SignatureType.EIP1271.header ->
            Cacao.Signature(type.header, EIP191Signer.sign(message.toByteArray(), privateKey).toCacaoSignature()).run {
                clazz.constructors.first().kotlinFunction?.call(t, s, m) as SignatureInterface
            }
        else -> throw Throwable("SignatureType not recognized")
    }

fun <T : SignatureInterface> KFunction<T>.hasCorrectOrderedParametersInConstructor(): Boolean =
    parameters.takeIf { it.size == 3 }?.run {
        val stringType = String::class.createType(nullable = false)
        val nullableStringType = String::class.createType(nullable = true)

        val tExists = this.getOrNull(0)?.run { type == stringType && name == "t" } ?: false
        val sExists = this.getOrNull(1)?.run { type == stringType && name == "s" } ?: false
        val mExists = this.getOrNull(2)?.run { type == nullableStringType && name == "m" } ?: false

        tExists && sExists && mExists
    } ?: false