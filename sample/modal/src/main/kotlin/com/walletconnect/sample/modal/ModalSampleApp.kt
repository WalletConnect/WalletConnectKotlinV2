package com.walletconnect.sample.modal

import android.app.Application
import com.google.firebase.appdistribution.FirebaseAppDistribution
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.sample.common.BuildConfig
import com.walletconnect.sample.common.RELAY_URL
import com.walletconnect.sample.common.tag
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.randomBytes
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.presets.Web3ModalChainsPresets
import com.walletconnect.web3.modal.utils.EthUtils
import timber.log.Timber

class ModalSampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val serverUri = "wss://$RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"
        val appMetaData = Core.Model.AppMetaData(
            name = "Kotlin Modals",
            description = "Kotlin Modals Lab Sample",
            url = "https://web3modal.com/",
            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
            redirect = "kotlin-modal-wc://request"
        )

        CoreClient.initialize(
            relayServerUrl = serverUri,
            connectionType = ConnectionType.AUTOMATIC,
            application = this,
            metaData = appMetaData,
        ) {
            Timber.e(it.throwable)
        }

        Web3Modal.initialize(Modal.Params.Init(core = CoreClient)) { error ->
            Timber.e(tag(this), error.throwable.stackTraceToString())
            Firebase.crashlytics.recordException(error.throwable)
        }

        Web3Modal.setChains(Web3ModalChainsPresets.ethChains.values.toList())

        val authParams = Modal.Model.AuthPayloadParams(
            chains = Web3ModalChainsPresets.ethChains.values.toList().map { it.id },
            domain = "sample.kotlin.modal",
            uri = "https://web3inbox.com/all-apps",
            nonce = randomBytes(12).bytesToHex(),
            statement = "I accept the Terms of Service: https://yourDappDomain.com/",
            resources = listOf(
                "urn:recap:eyJhdHQiOnsiaHR0cHM6Ly9ub3RpZnkud2FsbGV0Y29ubmVjdC5jb20vYWxsLWFwcHMiOnsiY3J1ZC9zdWJzY3JpcHRpb25zIjpbe31dLCJjcnVkL25vdGlmaWNhdGlvbnMiOlt7fV19fX0=",
                "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/"
            ),
            methods = EthUtils.ethMethods,
        )
        Web3Modal.setAuthRequestParams(authParams)

        FirebaseAppDistribution.getInstance().updateIfNewReleaseAvailable()
    }
}
