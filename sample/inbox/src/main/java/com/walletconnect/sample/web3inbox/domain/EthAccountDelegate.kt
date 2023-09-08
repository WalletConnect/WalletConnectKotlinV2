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
            get() = "0xE408Ec5ACF27fE24C4adcAC1d1Fa0E02F23d9033"
        override val privateKey: String
            get() = "ff0138a3fece4629dd63e211341edba8f5cd9d38ad8b5fee60739b8e63ed1aa0"
        override val publicKey: String
            get() = "204b9580416b72b18a6e19f8399e465acf43b11cc928ba531115a9b5c2a7d3731362b3cabb73e494b2ed4bad4827838816f90f1c0807327e95586637ae33499e"
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