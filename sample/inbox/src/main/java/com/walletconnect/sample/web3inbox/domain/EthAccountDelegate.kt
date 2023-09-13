package com.walletconnect.sample.web3inbox.domain

import android.content.Context
import com.walletconnect.foundation.common.model.PrivateKey
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
        // Old
//        override val address: String
//            get() = "0xB75e2Ae7fEC9cc2796F9C41377ed26095c5bFF36"
//        override val privateKey: String
//            get() = "56707c63ddb6401bfa3418f323ca07d3efd6cd539626346d8af8c6516fe50971"
//        override val publicKey: String
//            get() = "92e998f9af5cf488f48a8a35a6d9f09111bc74c52d76d65bccea05eb2a3c5a4468d5e32469d4be501701b04c3a274a3ddc855a6432f2a44b602530d1998a07a9"


        // New
//        override val address: String
//            get() = "0xdb5BB51Ed98fbd599273c352C68e948253bD8356"
//        override val privateKey: String
//            get() = "e2969b522ccadf6cf1870fe81bc4edb499aa21dafc4ceda3afc053209fa88371"
//        override val publicKey: String
//            get() = "aca1622a4d2ced3e74b36776b76ed735304fdcb5aee26ba8a7aab7c46bec3c8b28"

        // Swift
        override val address: String
            get() = "0xF625748d5929D09c85741807a05a9527f8B31625"
        override val privateKey: String
            get() = "b5bc0acbebbd3fb0200c3a1d4d089d3b3fe51047d9d16148e27b078368eb33ba"
        override val publicKey: String
            get() = "64117a637f5548c4de0fbf7b312dcf3fcd150abc22ef77490a771082fddef18562f9c0afd6468d2fd47f3b9d4f70d5eb0fd5566d77bde03f0b1e6eaffae88713"
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
//            val keypair = ECKeyPair.create(PrivateKey("b5bc0acbebbd3fb0200c3a1d4d089d3b3fe51047d9d16148e27b078368eb33ba").keyAsBytes)
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