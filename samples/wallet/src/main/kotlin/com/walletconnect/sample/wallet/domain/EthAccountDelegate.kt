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
//    val keypair = Keys.createEcKeyPair()
//    val publicKey = keypair.publicKey.toByteArray().bytesToHex()
//    val privateKey = keypair.privateKey.toByteArray().bytesToHex()
    // Swift
    val privateKey = "4dc0055d1831f7df8d855fc8cd9118f4a85ddc05395104c4cb0831a6752621a8"
    val publicKey = "62eb0b60b90e6ab0fbaaa897256dc60ff18f5354b3508796ac7be965eda6105a5537e74478da099e4e39f6ae56e58cb3916bd014a1a94997515976bb522f3a04"

//    val privateKey = "de15cb11963e9bde0a5cce06a5ee2bda1cf3a67be6fbcd7a4fc8c0e4c4db0298"
//    val publicKey = "7285ef9629c835310ffe6f76f282b03833cffb9a930e7a73efa09bd42d9c412ec0fa797197e296282fc7f4db387d28261f77d86c8fd36b72efd811555d928ff0"
    // JS

//    return Triple(publicKey, privateKey, Keys.toChecksumAddress(Keys.getAddress(keypair)))
    return Triple(publicKey, privateKey, Keys.toChecksumAddress(Keys.getAddress(publicKey)))

}

context(EthAccountDelegate)
fun String.toEthAddress(): String = "eip155:1:$this"