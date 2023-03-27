package com.walletconnect.sample_common

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.Keys
import java.security.Security


fun generateEthereumAccount(address: String) = "eip155:1:0x$address"

fun generateKeys(): Triple<String, String, String> {
    Security.getProviders().forEach { provider ->
        if (provider.name == "BC") {
            Security.removeProvider(provider.name)
        }
    }
    Security.addProvider(BouncyCastleProvider())
    val keypair = Keys.createEcKeyPair()
    val publicKey = keypair.publicKey.toByteArray().bytesToHex()
    val privateKey = keypair.privateKey.toByteArray().bytesToHex()
    return Triple(publicKey, privateKey, Keys.getAddress(keypair))
}

fun ByteArray.bytesToHex(): String {
    val hexString = StringBuilder(2 * this.size)

    this.indices.forEach { i ->
        val hex = Integer.toHexString(0xff and this[i].toInt())

        if (hex.length == 1) {
            hexString.append('0')
        }

        hexString.append(hex)
    }

    return hexString.toString()
}