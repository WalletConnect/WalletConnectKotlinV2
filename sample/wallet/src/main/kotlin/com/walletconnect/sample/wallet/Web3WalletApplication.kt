package com.walletconnect.sample.wallet

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.pandulapeter.beagle.modules.DividerModule
import com.pandulapeter.beagle.modules.HeaderModule
import com.pandulapeter.beagle.modules.PaddingModule
import com.pandulapeter.beagle.modules.TextModule
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.utils.cacao.sign
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.notify.client.cacao.CacaoSigner
import com.walletconnect.sample.common.RELAY_URL
import com.walletconnect.sample.common.initBeagle
import com.walletconnect.sample.common.tag
import com.walletconnect.sample.wallet.domain.EthAccountDelegate
import com.walletconnect.sample.wallet.domain.toEthAddress
import com.walletconnect.sample.wallet.ui.state.ConnectionState
import com.walletconnect.sample.wallet.ui.state.connectionStateFlow
import com.walletconnect.util.hexToBytes
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class Web3WalletApplication : Application() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        EthAccountDelegate.application = this
        Log.d(tag(this), "Account: ${EthAccountDelegate.account}")

        val projectId = BuildConfig.PROJECT_ID
        val serverUrl = "wss://$RELAY_URL?projectId=${projectId}"
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

        NotifyClient.initialize(
            init = Notify.Params.Init(CoreClient)
        ) { error ->
            Firebase.crashlytics.recordException(error.throwable)
            Log.e(tag(this), error.throwable.stackTraceToString())
        }

        // Register with Notify
        NotifyClient.register(
            params = Notify.Params.Registration(
                with(EthAccountDelegate) { account.toEthAddress() },
                domain = BuildConfig.APPLICATION_ID,
                onSign = { message -> CacaoSigner.sign(message, EthAccountDelegate.privateKey.hexToBytes(), SignatureType.EIP191) }
            ),
            onSuccess = {
                Log.e(tag(this), "Register Success")
            },
            onError = {
                Log.e(tag(this), it.throwable.stackTraceToString())
            }
        )

        initBeagle(
            this,
            HeaderModule(
                title = getString(R.string.app_name),
                subtitle = BuildConfig.APPLICATION_ID,
                text = "${BuildConfig.BUILD_TYPE} v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            ),
            DividerModule(),
            TextModule(text = with(EthAccountDelegate) { account.toEthAddress() }) {
                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Account", with(EthAccountDelegate) { account.toEthAddress() }))
            },
            PaddingModule(size = PaddingModule.Size.LARGE),
            TextModule(text = CoreClient.Echo.clientId) {
                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("ClientId", CoreClient.Echo.clientId))
            },
//            DividerModule(),
//            TextInputModule(
//                text = "Import Private Key",
//                validator = { text ->
//                    !text.startsWith("0x") && text.length == 64
//                },
//                onValueChanged = { text ->
//                    EthAccountDelegate.privateKey = text
//
//
//                    NotifyClient.register(
//                        params = Notify.Params.Registration(
//                            with(EthAccountDelegate) { account.toEthAddress() },
//                            isLimited = false,
//                            domain = BuildConfig.APPLICATION_ID,
//                            onSign = { message -> CacaoSigner.sign(message, EthAccountDelegate.privateKey.hexToBytes(), SignatureType.EIP191) }
//                        ),
//                        onSuccess = {
//                            Log.e(tag(this), "Register Success")
//                        },
//                        onError = {
//                            Log.e(tag(this), it.throwable.stackTraceToString())
//                        }
//                    )
//                }
//            )
        )

//        mixPanel = MixpanelAPI.getInstance(this, CommonBuildConfig.MIX_PANEL, true).apply {
//            identify(CoreClient.Echo.clientId)
//            people.set("\$name", with(EthAccountDelegate) { account.toEthAddress() })
//        }

        wcKoinApp.koin.get<Timber.Forest>().plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//                mixPanel.track(message)
            }
        })

        // For testing purposes only
        FirebaseMessaging.getInstance().deleteToken().addOnSuccessListener {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                Log.d(tag(this), token)
            }
        }
    }
}