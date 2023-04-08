@file:JvmName("CacaoSignerUtil")
@file:Suppress("PackageDirectoryMismatch", "UNCHECKED_CAST") // Added to dismiss confusion. Cast to `T` always succeeds as Cacao.Signature implements ISignature.

package com.walletconnect.android.utils.cacao

import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.android.cacao.signature.ISignatureType
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.android.internal.common.cacao.eip191.EIP191Signer
import com.walletconnect.android.internal.common.cacao.signature.toCacaoSignature
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType

interface CacaoSignerInterface<CoreSignature : SignatureInterface>

@Suppress("unused")
inline fun <CoreSignature : SignatureInterface, reified SDKSignature : CoreSignature> CacaoSignerInterface<CoreSignature>.sign(
    message: String,
    privateKey: ByteArray,
    type: ISignatureType,
): CoreSignature =
    when (type.header) {
        SignatureType.EIP191.header, SignatureType.EIP1271.header ->
            Cacao.Signature(type.header, EIP191Signer.sign(message.toByteArray(), privateKey).toCacaoSignature()).run {
                SDKSignature::class.constructors.first(KFunction<SDKSignature>::hasCorrectOrderedParametersInConstructor).call(t, s, m)
            }
        else -> throw Throwable("SignatureType not recognized")
    }

fun <T : SignatureInterface> sign(clazz: Class<T>, message: String, privateKey: ByteArray, type: ISignatureType): T =
    when (type.header) {
        SignatureType.EIP191.header, SignatureType.EIP1271.header ->
            Cacao.Signature(type.header, EIP191Signer.sign(message.toByteArray(), privateKey).toCacaoSignature()).run {
                clazz.kotlin.constructors.first(KFunction<T>::hasCorrectOrderedParametersInConstructor).call(t, s, m)
            }
        else -> throw Throwable("SignatureType not recognized")
    }

// This function is used to check if the constructor of the SignatureInterface impl has
// * exactly 3 parameters
// * the parameters in the correct order
// * the parameters are of the exact names that match the interface
fun <T : SignatureInterface> KFunction<T>.hasCorrectOrderedParametersInConstructor(): Boolean =
    parameters.takeIf { it.size == 3 }?.run {
        val stringType = String::class.createType(nullable = false).javaClass
        val nullableStringType = String::class.createType(nullable = true).javaClass

        // Check each parameter in the constructor to see if it matches the expected type and name.
        // The name of the parameter will either be the exact name when T is a data class, or arg{N} when T is a java class.
        val tParameterExists = this.getOrNull(0)?.run { type.javaClass == stringType && name in listOf("t", "arg0") } ?: false
        val sParameterExists = this.getOrNull(1)?.run { type.javaClass == stringType && name in listOf("s", "arg1") } ?: false
        val mParameterExists = this.getOrNull(2)?.run { type.javaClass == nullableStringType && name in listOf("m", "arg2") } ?: false

        tParameterExists && sParameterExists && mParameterExists
    } ?: false