package com.walletconnect.sample.wallet.domain

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.Keys
import java.security.Security

object EthAccountDelegate {
    lateinit var application: Application
    private val sharedPreferences: SharedPreferences by lazy { application.getSharedPreferences("Web3Inbox_Shared_Prefs", Context.MODE_PRIVATE) }
    private val newlyGeneratedKeys: Triple<String, String, String> by lazy { generateKeys() }
    private const val ACCOUNT_TAG = "self_account_tag"
    private const val PRIVATE_KEY_TAG = "self_private_key"
    private const val PUBLIC_KEY_TAG = "self_public_key"

    val account: String
        get() = sharedPreferences.getString(ACCOUNT_TAG, null) ?: "0x${newlyGeneratedKeys.third}".also {
            sharedPreferences.edit { putString(ACCOUNT_TAG, it) }
        }
    val privateKey: String
        get() = sharedPreferences.getString(PRIVATE_KEY_TAG, null) ?: newlyGeneratedKeys.second.also {
            sharedPreferences.edit { putString(PRIVATE_KEY_TAG, it) }
        }
    val publicKey: String
        get() = sharedPreferences.getString(PUBLIC_KEY_TAG, null) ?: newlyGeneratedKeys.first.also {
            sharedPreferences.edit { putString(PUBLIC_KEY_TAG, it) }
        }
}

private fun generateKeys(): Triple<String, String, String> {
    Security.getProviders().forEach { provider ->
        if (provider.name == "BC") {
            Security.removeProvider(provider.name)
        }
    }
    Security.addProvider(BouncyCastleProvider())
    val keypair = Keys.createEcKeyPair()
    val publicKey = keypair.publicKey.toByteArray().bytesToHex()//.run { padStart(130, '0') }
    val privateKey = keypair.privateKey.toByteArray().bytesToHex()//.run { padStart(66, '0') }

    return Triple(publicKey, privateKey, Keys.getAddress(keypair))
}

context(EthAccountDelegate)
fun String.toEthAddress(): String = "eip155:1:$this"