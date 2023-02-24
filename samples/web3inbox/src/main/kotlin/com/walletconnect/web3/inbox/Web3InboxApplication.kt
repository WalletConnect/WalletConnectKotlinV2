package com.walletconnect.web3.inbox

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.sign
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.sample_common.generateEthereumAccount
import com.walletconnect.sample_common.generateKeys
import com.walletconnect.util.hexToBytes
import com.walletconnect.web3.inbox.cacao.CacaoSigner
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.Web3Inbox
import com.walletconnect.web3.inbox.sample.BuildConfig
import timber.log.Timber
import timber.log.Timber.DebugTree


class Web3InboxApplication : Application() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var _account: String
    private lateinit var _publicKey: String
    private lateinit var _privateKey: String

    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        getAccount()
        Timber.d("Account: $_account")

        val projectId = BuildConfig.PROJECT_ID
        val relayUrl = "relay.walletconnect.com"
        val serverUrl = "wss://$relayUrl?projectId=${projectId}"
        val appMetaData = Core.Model.AppMetaData(
            name = "Kotlin.Web3Inbox",
            description = "Kotlin Web3Inbox Implementation",
            url = "kotlin.web3inbox.walletconnect.com",
            icons = listOf("https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png"),
            redirect = null
        )

        CoreClient.initialize(
            relayServerUrl = serverUrl,
            connectionType = ConnectionType.AUTOMATIC,
            application = this,
            metaData = appMetaData
        ) { error ->
            Timber.tag("CoreClient").e(error.throwable)
        }

        Web3Inbox.initialize(Inbox.Params.Init(core = CoreClient, account = Inbox.Type.AccountId(_account),
            onSign = { message -> CacaoSigner.sign(message, _privateKey.hexToBytes(), SignatureType.EIP191) }
        )) { error ->
            Timber.tag("Web3Inbox").e(error.throwable)
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