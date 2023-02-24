package com.walletconnect.sample_common

import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.util.bytesToHex
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.Keys
import java.security.Security


fun generateEthereumAccount(address: String) = "eip155:1:0x$address"

fun generateKeys(): Triple<String, String, String> {
    Security.removeProvider("BC")
    Security.addProvider(BouncyCastleProvider())
    val keypair = Keys.createEcKeyPair()
    val publicKey = PublicKey(keypair.publicKey.toByteArray().bytesToHex())
    val privateKey = PrivateKey(keypair.privateKey.toByteArray().bytesToHex())
    return Triple(publicKey.keyAsHex, privateKey.keyAsHex, Keys.getAddress(keypair))
}