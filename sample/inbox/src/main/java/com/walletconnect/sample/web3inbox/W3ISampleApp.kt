package com.walletconnect.sample.web3inbox

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.sample.web3inbox.domain.EthAccount
import com.walletconnect.sample.web3inbox.domain.SharedPrefStorage
import com.walletconnect.sample.web3inbox.domain.WCMDelegate
import com.walletconnect.sample.web3inbox.domain.Web3InboxInitializer
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import timber.log.Timber
import java.util.concurrent.TimeUnit

class W3ISampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        val serverUri = "wss://relay.walletconnect.com?projectId=${BuildConfig.PROJECT_ID}"
        val appMetaData = Core.Model.AppMetaData(
            name = "Kotlin W3I Sample",
            description = "Kotlin W3I Sample Implementation",
            url = "kotlin.sample.w3i.walletconnect.com",
            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
            redirect = "kotlin-sample-w3i-wc://request"
        )
        val sessionParams = Modal.Params.SessionParams(
            requiredNamespaces = mapOf(
                "eip155" to Modal.Model.Namespace.Proposal(
                    chains = listOf("eip155:1"),
                    methods = listOf("eth_sendTransaction", "personal_sign", "eth_sign", "eth_signTypedData"),
                    events = emptyList()
                )
            ),
            properties = mapOf("sessionExpiry" to ((System.currentTimeMillis() / 1000) + TimeUnit.SECONDS.convert(7, TimeUnit.DAYS)).toString())
        )

        CoreClient.initialize(
            relayServerUrl = serverUri,
            connectionType = ConnectionType.AUTOMATIC,
            application = this,
            metaData = appMetaData,
        ) { error -> Timber.e(error.throwable) }

        FirebaseMessaging.getInstance().deleteToken().addOnSuccessListener {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {}
        }

        SharedPrefStorage.getLastLoggedInAccount(this)?.let { account ->
            Timber.d("Last logged in: $account")
            Web3InboxInitializer.init(account, EthAccount.Random(this))
        } ?: run {
            Timber.d("No last logged in account")
        }

        WalletConnectModal.initialize(
            Modal.Params.Init(core = CoreClient, sessionParams = sessionParams),
            onSuccess = { WalletConnectModal.setDelegate(WCMDelegate) },
            onError = { error -> Timber.e(error.throwable) }
        )
    }
}