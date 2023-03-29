package com.walletconnect.sample.wallet

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.sign
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletClient
import com.walletconnect.sample_common.generateEthereumAccount
import com.walletconnect.sample_common.generateKeys
import com.walletconnect.sample_common.tag
import com.walletconnect.util.hexToBytes
import com.walletconnect.web3.inbox.cacao.CacaoSigner
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.Web3Inbox
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet

class Web3WalletApplication : Application() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var _account: String
    private lateinit var _publicKey: String
    private lateinit var _privateKey: String

    override fun onCreate() {
        super.onCreate()
        getAccount()
        Log.d(tag(this),"Account: $_account")

        val projectId = BuildConfig.PROJECT_ID
        val relayUrl = "relay.walletconnect.com"
        val serverUrl = "wss://$relayUrl?projectId=${projectId}"
        val appMetaData = Core.Model.AppMetaData(
            name = "Kotlin Wallet",
            description = "Kotlin Wallet Implementation",
            url = "kotlin.wallet.walletconnect.com",
            icons = listOf("https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png"),
            redirect = "kotlin-web3wallet:/request"
        )

        CoreClient.initialize(
            relayServerUrl = serverUrl,
            connectionType = ConnectionType.AUTOMATIC,
            application = this,
            metaData = appMetaData
        ) { error ->
            Firebase.crashlytics.recordException(error.throwable)
        }

        Web3Wallet.initialize(Wallet.Params.Init(core = CoreClient)) { error ->
            Firebase.crashlytics.recordException(error.throwable)
        }

        PushWalletClient.initialize(Push.Wallet.Params.Init(CoreClient)) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }

        Web3Inbox.initialize(Inbox.Params.Init(core = CoreClient, account = Inbox.Type.AccountId(_account),
            onSign = { message -> CacaoSigner.sign(message, _privateKey.hexToBytes(), SignatureType.EIP191) }
        )) { error ->
            Firebase.crashlytics.recordException(error.throwable)
        }

        // For testing purposes only
        FirebaseMessaging.getInstance().deleteToken().addOnSuccessListener {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                Log.d(tag(this), token)
            }
        }
    }


    private fun getAccount() {
        sharedPreferences = getSharedPreferences("Web3Inbox_Shared_Prefs", Context.MODE_PRIVATE)
        val account = sharedPreferences.getString(ACCOUNT_TAG, null)
        val publicKey = sharedPreferences.getString(PUBLIC_KEY_TAG, null)
        val privateKey = sharedPreferences.getString(PRIVATE_KEY_TAG, null)

        if (account != null && publicKey != null && privateKey != null) {
            _account = account
            _publicKey = publicKey
            _privateKey = privateKey
            // Note: This is only demo. Normally you want more security with private key
        } else {
            val keypair = generateKeys()
            _publicKey = keypair.first
            _privateKey = keypair.second
            _account = generateEthereumAccount(keypair.third)

            sharedPreferences.edit {
                putString(ACCOUNT_TAG, _account)
                putString(PUBLIC_KEY_TAG, _publicKey)
                putString(PRIVATE_KEY_TAG, _privateKey)
            }
        }
    }

    private companion object {
        const val ACCOUNT_TAG = "self_account_tag"
        const val PRIVATE_KEY_TAG = "self_private_key"
        const val PUBLIC_KEY_TAG = "self_public_key"
    }
}