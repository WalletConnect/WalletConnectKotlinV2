@file:JvmSynthetic

package com.walletconnect.web3.wallet.utils

import com.walletconnect.auth.signature.eip191.EIP191Signer
import com.walletconnect.auth.signature.toCacaoSignature
import com.walletconnect.web3.wallet.client.Wallet

object CacaoSigner {
    fun sign(message: ByteArray, privateKey: ByteArray, type: SignatureType): Wallet.Model.Cacao.Signature =
        when (type) {
            SignatureType.EIP191, SignatureType.EIP1271 ->
                Wallet.Model.Cacao.Signature(type.header, EIP191Signer.sign(message, privateKey).toCacaoSignature())
        }

    fun sign(message: String, privateKey: ByteArray, type: SignatureType): Wallet.Model.Cacao.Signature =
        sign(message.toByteArray(), privateKey, type)
}