package com.walletconnect.web3.inbox.domain

import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.util.bytesToHex
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.Keys
import java.security.Security

internal fun generateEthereumAccount(address: String) = "eip155:1:0x$address"

internal fun generateKeys(): Triple<String, String, String> {
    Security.removeProvider("BC")
    Security.addProvider(BouncyCastleProvider())
    val keypair = Keys.createEcKeyPair()
    val publicKey = PublicKey(keypair.publicKey.toByteArray().bytesToHex())
    val privateKey = PrivateKey(keypair.privateKey.toByteArray().bytesToHex())
    return Triple(publicKey.keyAsHex, privateKey.keyAsHex, Keys.getAddress(keypair))
}

internal const val ACCOUNT_TAG = "self_account_tag"
internal const val PRIVATE_KEY_TAG = "self_private_key"
internal const val PUBLIC_KEY_TAG = "self_public_key"