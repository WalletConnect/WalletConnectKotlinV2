package com.walletconnect.sample.wallet

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Placement
import com.pandulapeter.beagle.modules.DividerModule
import com.pandulapeter.beagle.modules.HeaderModule
import com.pandulapeter.beagle.modules.PaddingModule
import com.pandulapeter.beagle.modules.TextInputModule
import com.pandulapeter.beagle.modules.TextModule
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.utils.cacao.sign
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.notify.client.cacao.CacaoSigner
import com.walletconnect.sample.common.RELAY_URL
import com.walletconnect.sample.common.initBeagle
import com.walletconnect.sample.common.tag
import com.walletconnect.sample.wallet.domain.EthAccountDelegate
import com.walletconnect.sample.wallet.domain.NotificationHandler
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import com.walletconnect.sample.wallet.domain.mixPanel
import com.walletconnect.sample.wallet.domain.toEthAddress
import com.walletconnect.sample.wallet.ui.state.ConnectionState
import com.walletconnect.sample.wallet.ui.state.connectionStateFlow
import com.walletconnect.util.hexToBytes
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.qualifier.named
import timber.log.Timber
import com.walletconnect.sample.common.BuildConfig as CommonBuildConfig

class Web3WalletApplication : Application() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var addFirebaseBeagleModules: () -> Unit = {}
    private lateinit var logger: Logger

    override fun onCreate() {
        super.onCreate()

        EthAccountDelegate.application = this

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
            logger.error(error.throwable.stackTraceToString())
            scope.launch {
                connectionStateFlow.emit(ConnectionState.Error(error.throwable.message ?: ""))
            }
        }

        mixPanel = MixpanelAPI.getInstance(this, CommonBuildConfig.MIX_PANEL, true).apply {
            identify(CoreClient.Push.clientId)
            people.set("\$name", with(EthAccountDelegate) { account.toEthAddress() })
        }

        logger = wcKoinApp.koin.get(named(AndroidCommonDITags.LOGGER))
        logger.log("Account: ${EthAccountDelegate.account}")

        Web3Wallet.initialize(Wallet.Params.Init(core = CoreClient)) { error ->
            Firebase.crashlytics.recordException(error.throwable)
            logger.error(error.throwable.stackTraceToString())
        }

        NotifyClient.initialize(
            init = Notify.Params.Init(CoreClient)
        ) { error ->
            Firebase.crashlytics.recordException(error.throwable)
            logger.error(error.throwable.stackTraceToString())
        }

        registerAccount()
        initializeBeagle()



        wcKoinApp.koin.get<Timber.Forest>().plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                println("kobe: Message: $message; Throwable: $t")
                mixPanel.track(message)
            }
        })


        // For testing purposes only
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            addFirebaseBeagleModules = {

                Web3Wallet.registerDeviceToken(firebaseAccessToken = token, enableEncrypted = true,
                    onSuccess = {
                        Timber.tag(tag(this)).e("Successfully registered firebase token for Web3Wallet")
                    },
                    onError = {
                        logger.error("Error while registering firebase token for Web3Wallet: ${it.throwable}")
                        Firebase.crashlytics.recordException(Throwable("Error while registering firebase token for Web3Wallet: ${it.throwable}"))
                    })

                Beagle.add(
                    PaddingModule(size = PaddingModule.Size.LARGE, id = "${token}Padding"),
                    placement = Placement.Below(id = CoreClient.Push.clientId)
                )
                Beagle.add(
                    TextModule(text = token) {
                        (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("FMC_Token", token))
                    },
                    placement = Placement.Below(id = "${token}Padding")
                )
            }
            addFirebaseBeagleModules()
        }

        handleNotifyMessages()
    }

    private fun initializeBeagle() {
        initBeagle(
            this@Web3WalletApplication,
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
            TextModule(text = with(EthAccountDelegate) { privateKey }) {
                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Private Key", with(EthAccountDelegate) { privateKey }))
            },
            PaddingModule(size = PaddingModule.Size.LARGE),
            TextModule(text = CoreClient.Push.clientId, id = CoreClient.Push.clientId) {
                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("ClientId", CoreClient.Push.clientId))
            },
            DividerModule(),
            TextInputModule(
                text = "Import Private Key",
                areRealTimeUpdatesEnabled = false,
                validator = { text ->
                    !text.startsWith("0x") && text.length == 64
                },
                onValueChanged = { text ->
                    NotifyClient.unregister(
                        params = Notify.Params.Unregistration(
                            with(EthAccountDelegate) { account.toEthAddress() },
                        ),
                        onSuccess = {
                            logger.log("Unregister Success")

                            EthAccountDelegate.privateKey = text


                            registerAccount()
                        },
                        onError = { logger.error(it.throwable.stackTraceToString()) }
                    )
                }
            ),
        )
        addFirebaseBeagleModules()
    }


    private fun handleNotifyMessages() {
        val scope = CoroutineScope(Dispatchers.Default)

        val notifyEventsJob = NotifyDelegate.notifyEvents
            .filterIsInstance<Notify.Event.Notification>()
            .onEach { notification -> NotificationHandler.addNotification(notification.notification.message) }
            .launchIn(scope)


        val notificationDisplayingJob = NotificationHandler.startNotificationDisplayingJob(scope, this)


        notifyEventsJob.invokeOnCompletion { cause ->
            onScopeCancelled(cause, "notifyEventsJob")
        }

        notificationDisplayingJob.invokeOnCompletion { cause ->
            onScopeCancelled(cause, "notificationDisplayingJob")
        }
    }

    private fun onScopeCancelled(error: Throwable?, job: String) {
        wcKoinApp.koin.get<Logger>(named(AndroidCommonDITags.LOGGER)).error("onScopeCancelled($job): $error")
    }

    private fun registerAccount() {
        val account = with(EthAccountDelegate) { account.toEthAddress() }
        val domain = BuildConfig.APPLICATION_ID
        val allApps = true

        val isRegistered = NotifyClient.isRegistered(params = Notify.Params.IsRegistered(account = account, domain = domain, allApps = allApps))

        if (!isRegistered) {
            NotifyClient.prepareRegistration(
                params = Notify.Params.PrepareRegistration(account = account, domain = domain, allApps = allApps),
                onSuccess = { cacaoPayloadWithIdentityPrivateKey, message ->
                    logger.log("PrepareRegistration Success")

                    val signature = CacaoSigner.sign(message, EthAccountDelegate.privateKey.hexToBytes(), SignatureType.EIP191)

                    NotifyClient.register(
                        params = Notify.Params.Register(cacaoPayloadWithIdentityPrivateKey = cacaoPayloadWithIdentityPrivateKey, signature = signature),
                        onSuccess = { logger.log("Register Success") },
                        onError = { logger.error(it.throwable.stackTraceToString()) }
                    )

                },
                onError = { logger.error(it.throwable.stackTraceToString()) }
            )
        } else {
            logger.log("$account is already registered")
        }
    }

}