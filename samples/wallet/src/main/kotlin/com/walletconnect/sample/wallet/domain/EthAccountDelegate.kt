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
    private val sharedPreferences: SharedPreferences by lazy { application.getSharedPreferences("Wallet_Sample_Shared_Prefs", Context.MODE_PRIVATE) }
    private const val ACCOUNT_TAG = "self_account_tag"
    private const val PRIVATE_KEY_TAG = "self_private_key"
    private const val PUBLIC_KEY_TAG = "self_public_key"

    private val isInitialized
        get() = (sharedPreferences.getString(ACCOUNT_TAG, null) != null) && (sharedPreferences.getString(PRIVATE_KEY_TAG, null) != null) && (sharedPreferences.getString(PUBLIC_KEY_TAG, null) != null)

    private fun storeAccount(): Triple<String, String, String> = generateKeys().also { (publicKey, privateKey, address) ->
        sharedPreferences.edit { putString(ACCOUNT_TAG, address) }
        sharedPreferences.edit { putString(PRIVATE_KEY_TAG, privateKey) }
        sharedPreferences.edit { putString(PUBLIC_KEY_TAG, publicKey) }
    }

    val account: String
        get() = if (isInitialized) sharedPreferences.getString(ACCOUNT_TAG, null)!! else storeAccount().third

    val privateKey: String
        get() = if (isInitialized) sharedPreferences.getString(PRIVATE_KEY_TAG, null)!! else storeAccount().second

    val publicKey: String
        get() = if (isInitialized) sharedPreferences.getString(PUBLIC_KEY_TAG, null)!! else storeAccount().first

}

context(EthAccountDelegate)
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
    return Triple(publicKey, privateKey, Keys.toChecksumAddress(Keys.getAddress(keypair)))
}

context(EthAccountDelegate)
fun String.toEthAddress(): String = "eip155:1:$this"