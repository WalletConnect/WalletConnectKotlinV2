package com.walletconnect.sample.wallet

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Text
import com.pandulapeter.beagle.common.configuration.toText
import com.pandulapeter.beagle.common.contracts.BeagleListItemContract
import com.pandulapeter.beagle.modules.DividerModule
import com.pandulapeter.beagle.modules.HeaderModule
import com.pandulapeter.beagle.modules.ItemListModule
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.utils.cacao.sign
import com.walletconnect.sample.common.initBeagle
import com.walletconnect.sample.common.tag
import com.walletconnect.sample.wallet.domain.EthAccountDelegate
import com.walletconnect.sample.wallet.domain.toEthAddress
import com.walletconnect.sample.wallet.ui.state.ConnectionState
import com.walletconnect.sample.wallet.ui.state.connectionStateFlow
import com.walletconnect.util.hexToBytes
import com.walletconnect.web3.inbox.cacao.CacaoSigner
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.Web3Inbox
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class Web3WalletApplication : Application() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        initBeagle(
            this,
            HeaderModule(
                title = getString(R.string.app_name),
                subtitle = BuildConfig.APPLICATION_ID,
                text = "${BuildConfig.BUILD_TYPE} v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            ),
        )
        EthAccountDelegate.application = this
        Log.d(tag(this), "Account: ${EthAccountDelegate.account}")

        Beagle.add(
            DividerModule(),
            ItemListModule(
                Text.CharSequence("Account Info"),
                listOf(
                    object : BeagleListItemContract {
                        override val title: Text = with(EthAccountDelegate) { account.toEthAddress() }.toText()

                        override fun equals(other: Any?): Boolean = super.equals(other)

                        override fun hashCode(): Int = super.hashCode()
                    }/*,
                    object : BeagleListItemContract {
                        override val title: Text = EthAccountDelegate.publicKey.toText()

                        override fun equals(other: Any?): Boolean = super.equals(other)

                        override fun hashCode(): Int = super.hashCode()
                    },
                    object : BeagleListItemContract {
                        override val title: Text = EthAccountDelegate.privateKey.toText()

                        override fun equals(other: Any?): Boolean = super.equals(other)

                        override fun hashCode(): Int = super.hashCode()
                    }*/
                ),
                false,
            ) { item ->
                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(item.id, item.id))
            }
        )

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
            Log.e(tag(this), error.throwable.stackTraceToString())
            scope.launch {
                connectionStateFlow.emit(ConnectionState.Error(error.throwable.message ?: ""))
            }
        }

        Web3Wallet.initialize(Wallet.Params.Init(core = CoreClient)) { error ->
            Firebase.crashlytics.recordException(error.throwable)
            Log.e(tag(this), error.throwable.stackTraceToString())
        }

        Web3Inbox.initialize(Inbox.Params.Init(core = CoreClient, account = Inbox.Type.AccountId(with(EthAccountDelegate) { account.toEthAddress() }),
            onSign = { message ->
                Log.d(tag(this), message)
                CacaoSigner.sign(message, EthAccountDelegate.privateKey.hexToBytes(), SignatureType.EIP191)
            }
        )) { error ->
            Firebase.crashlytics.recordException(error.throwable)
            Log.e(tag(this), error.throwable.stackTraceToString())
        }

        // For testing purposes only
        FirebaseMessaging.getInstance().deleteToken().addOnSuccessListener {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                Log.d(tag(this), token)
            }
        }
    }
}