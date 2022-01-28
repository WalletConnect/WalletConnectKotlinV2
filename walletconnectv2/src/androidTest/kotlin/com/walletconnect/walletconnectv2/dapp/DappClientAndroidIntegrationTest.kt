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
        val initParams =
            WalletConnect.Params.Init(
                application = app,
                serverUrlConfig = WalletConnect.Params.ServerUrlConfig.Properties(
                    WalletConnect.Params.UrlProps(
                        hostName = "wss://relay.walletconnect.com/",
                        projectId = "2ee94aca5d98e6c05c38bce02bee952a",
                        useTls = true
                    )
                ),
                isController = false,
                metadata = WalletConnect.Model.AppMetaData(
                    name = "Kotlin Dapp",
                    description = "Dapp description",
                    url = "example.dapp",
                    icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
                )
            )
        WalletConnectClient.initialize(initParams)

        val connectParams = WalletConnect.Params.Connect(
            permissions = WalletConnect.Model.SessionPermissions(
                WalletConnect.Model.Blockchain(listOf("eip155:69")),
                WalletConnect.Model.Jsonrpc(listOf("personal_sign"))
            ), pairingTopic = null
        )

        WalletConnectClient.connect(connectParams)
    }

}
