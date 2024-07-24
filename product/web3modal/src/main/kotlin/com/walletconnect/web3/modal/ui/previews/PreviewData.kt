package com.walletconnect.web3.modal.ui.previews

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.domain.model.AccountData

private val metaMask: Wallet
    get() = Wallet(id = "1", name = "MetaMask", homePage = "", order = "", imageUrl = "", mobileLink = "metamask://", webAppLink = "", playStore = "", linkMode = "").apply { isRecent = true }
private val trustWallet: Wallet
    get() = Wallet(id = "2", name = "Trust Wallet", homePage = "", order = "", imageUrl = "", mobileLink = "trustwallet://", webAppLink = "", playStore = "", linkMode = "").apply {
        isWalletInstalled = true
    }
private val safe: Wallet
    get() = Wallet(id = "3", name = "Safe", homePage = "", order = "", imageUrl = "", mobileLink = "safe://", webAppLink = "", playStore = "", linkMode = "")
private val rainbow: Wallet
    get() = Wallet(id = "4", name = "Rainbow", homePage = "", order = "", imageUrl = "", mobileLink = "rainbow://", webAppLink = "", playStore = "", linkMode = "")
private val zerion: Wallet
    get() = Wallet(id = "5", name = "Zerion", homePage = "", order = "", imageUrl = "", mobileLink = "zerion://", webAppLink = "", playStore = "", linkMode = "")
private val argent: Wallet
    get() = Wallet(id = "6", name = "Argent", homePage = "", order = "", imageUrl = "", mobileLink = "argent://", webAppLink = "", playStore = "", linkMode = "")
private val spot: Wallet
    get() = Wallet(id = "7", name = "Spot", homePage = "", order = "", imageUrl = "", mobileLink = "spot://", webAppLink = "", playStore = "", linkMode = "")
private val imToken: Wallet
    get() = Wallet(id = "8", name = "imToken", homePage = "", order = "", imageUrl = "", mobileLink = "imtoken://", webAppLink = "", playStore = "", linkMode = "")
private val alphaWallet: Wallet
    get() = Wallet(id = "9", name = "AlphaWallet", homePage = "", order = "", imageUrl = "", mobileLink = "alphawallet://", webAppLink = "", playStore = "", linkMode = "")
private val omni: Wallet
    get() = Wallet(id = "10", name = "Omni", homePage = "", order = "", imageUrl = "", mobileLink = "omni://", webAppLink = "", playStore = "", linkMode = "")
private val bitkeep: Wallet
    get() = Wallet(id = "11", name = "BitKeep", homePage = "", order = "", imageUrl = "", mobileLink = "bitkeep://", webAppLink = "", playStore = "", linkMode = "")
private val tokenPocket: Wallet
    get() = Wallet(id = "12", name = "TokePocket", homePage = "", order = "", imageUrl = "", mobileLink = "tokenpocket://", webAppLink = "", playStore = "", linkMode = "")
private val ledgerLive: Wallet
    get() = Wallet(id = "13", name = "Ledger Live", homePage = "", order = "", imageUrl = "", mobileLink = "ledgerlive://", webAppLink = "", playStore = "", linkMode = "")
private val frontier: Wallet
    get() = Wallet(id = "14", name = "Frontier", homePage = "", order = "", imageUrl = "", mobileLink = "frontier://", webAppLink = "", playStore = "", linkMode = "")
private val safePal: Wallet
    get() = Wallet(id = "15", name = "SafePal", homePage = "", order = "", imageUrl = "", mobileLink = "safepal://", webAppLink = "", playStore = "", linkMode = "")

internal val testWallets: List<Wallet>
    get() = listOf(metaMask, trustWallet, safe, rainbow, zerion, argent, spot, imToken, alphaWallet, omni, bitkeep, tokenPocket, ledgerLive, frontier, safePal)

internal val accountDataPreview: AccountData
    get() = AccountData(
        address = "0xd2B8b483056b134f9D8cd41F55bB065F9",
        chains = testChains,
        identity = null
    )

internal class ConnectYourWalletPreviewProvider : PreviewParameterProvider<List<Wallet>> {
    override val values = sequenceOf(
        listOf(),
        testWallets.toList().take(3),
        testWallets.toList().apply { first().isWalletInstalled = true },
        testWallets.toList().apply { first().isRecent = true },
        testWallets.toList().map { it.apply { isWalletInstalled = true } },
        testWallets.toList()
    )
}

private val ethToken = Modal.Model.Token(name = "Ether", symbol = "ETH", decimal = 18)

internal val ethereumChain: Modal.Model.Chain
    get() = Modal.Model.Chain(
        chainName = "Ethereum",
        chainNamespace = "eip155",
        chainReference = "1",
        requiredMethods = listOf(),
        optionalMethods = listOf(),
        events = listOf(),
        token = ethToken,
        rpcUrl = "https://cloudflare-eth.com",
        blockExplorerUrl = "https://etherscan.io"
    )

internal val arbitrumChain: Modal.Model.Chain
    get() = Modal.Model.Chain(
        chainName = "Arbitrum One",
        chainNamespace = "eip155",
        chainReference = "42161",
        requiredMethods = listOf(),
        optionalMethods = listOf(),
        events = listOf(),
        token = ethToken,
        rpcUrl = "https://arb1.arbitrum.io/rpc",
        blockExplorerUrl = "https://arbiscan.io"
    )

internal val testChains: List<Modal.Model.Chain>
    get() = listOf(ethereumChain, arbitrumChain)
