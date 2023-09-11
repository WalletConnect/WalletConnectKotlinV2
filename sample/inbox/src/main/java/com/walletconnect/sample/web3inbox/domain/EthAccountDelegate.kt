package com.walletconnect.sample.web3inbox.domain

import android.content.Context
import com.walletconnect.util.bytesToHex
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import java.security.Security

// TODO Move to Common
sealed interface EthAccount {

    val address: String
    val privateKey: String
    val publicKey: String

    fun caip10(): String = address.toEthAddress()

    object Fixed : EthAccount {
        override val address: String
            get() = "0xB75e2Ae7fEC9cc2796F9C41377ed26095c5bFF36"
        override val privateKey: String
            get() = "56707c63ddb6401bfa3418f323ca07d3efd6cd539626346d8af8c6516fe50971"
        override val publicKey: String
            get() = "92e998f9af5cf488f48a8a35a6d9f09111bc74c52d76d65bccea05eb2a3c5a4468d5e32469d4be501701b04c3a274a3ddc855a6432f2a44b602530d1998a07a9"
    }

    class Random(context: Context) : EthAccount {
        private fun generateKeys(): Triple<String, String, String> {
            Security.getProviders().forEach { provider ->
                if (provider.name == "BC") {
                    Security.removeProvider(provider.name)
                }
            }
            Security.addProvider(BouncyCastleProvider())
            val keypair = Keys.createEcKeyPair()
            val publicKey = keypair.publicKey.toByteArray().bytesToHex()
            val privateKey = keypair.privateKey.toByteArray().bytesToHex()

            return Triple(publicKey, privateKey, Keys.toChecksumAddress(Keys.getAddress(keypair)))
        }

        private var _privateKey: String
        private var _publicKey: String
        private var _address: String

        init {
            if (SharedPrefStorage.isInitialized(context)) {
                _privateKey = SharedPrefStorage.getSavedRandomPrivateKey(context)!!
                _publicKey = SharedPrefStorage.getSavedRandomPublicKey(context)!!
                _address = SharedPrefStorage.getSavedRandomAccount(context)!!
            } else {
                val newKeys = generateKeys()
                _privateKey = newKeys.second
                _publicKey = newKeys.first
                _address = newKeys.third
                SharedPrefStorage.saveRandomPrivateKey(context, _privateKey)
                SharedPrefStorage.saveRandomPublicKey(context, _publicKey)
                SharedPrefStorage.saveRandomAccount(context, _address)
            }
        }

        override val address: String
            get() = _address
        override val privateKey: String
            get() = _privateKey
        override val publicKey: String
            get() = _publicKey
    }


    object Burner : EthAccount {
        private var keypair: ECKeyPair

        init {
            Security.getProviders().forEach { provider ->
                if (provider.name == "BC") {
                    Security.removeProvider(provider.name)
                }
            }
            Security.addProvider(BouncyCastleProvider())
            keypair = Keys.createEcKeyPair()
        }

        override val publicKey: String = keypair.publicKey.toByteArray().bytesToHex()
        override val privateKey: String = keypair.privateKey.toByteArray().bytesToHex()
        override val address: String = Keys.toChecksumAddress(Keys.getAddress(publicKey))
    }
}

fun String.toEthAddress(): String = "eip155:1:$this"