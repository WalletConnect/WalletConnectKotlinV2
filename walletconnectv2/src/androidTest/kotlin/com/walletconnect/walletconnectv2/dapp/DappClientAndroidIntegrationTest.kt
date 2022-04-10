package com.walletconnect.walletconnectv2.dapp

import androidx.test.core.app.ApplicationProvider
import com.walletconnect.walletconnectv2.WCIntegrationActivityScenarioRule
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import com.walletconnect.walletconnectv2.utils.IntegrationTestApplication
import org.junit.Rule
import org.junit.Test

class DappClientAndroidIntegrationTest {

    @get:Rule
    val activityRule = WCIntegrationActivityScenarioRule()
    private val app = ApplicationProvider.getApplicationContext<IntegrationTestApplication>()

    private val metadata = WalletConnect.Model.AppMetaData(
        name = "Kotlin Wallet",
        description = "Wallet description",
        url = "example.wallet",
        icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
    )

    @Test
    fun testDappConnectMethod() {
        val initParams = WalletConnect.Params.Init(
            application = app,
            useTls = true,
            hostName = "",
            projectId = "",
            metadata = WalletConnect.Model.AppMetaData(
                name = "Kotlin Dapp",
                description = "Dapp description",
                url = "example.dapp",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
            )
        )
        WalletConnectClient.initialize(initParams)

        WalletConnectClient.connect(
            WalletConnect.Params.Connect(
                permissions = WalletConnect.Model.SessionPermissions(jsonRpc = WalletConnect.Model.JsonRpc(listOf("eth_sign"))),
                blockchain = WalletConnect.Model.Blockchain(listOf("1", "2"))
            ),
            onProposedSequence = { proposedSequence ->
                when (proposedSequence) {
                    is WalletConnect.Model.ProposedSequence.Pairing -> {

                    }

                    is WalletConnect.Model.ProposedSequence.Session -> {

                    }
                }

            }
        )
    }
}