package com.walletconnect.web3.modal.ui.previews

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.client.Modal

private val metaMask: Wallet
    get() = Wallet(id = "1", name = "MetaMask", homePage = "", order = "", imageUrl = "", mobileLink = "metamask://", playStore = "")
private val trustWallet: Wallet
    get() = Wallet(id = "2", name = "Trust Wallet", homePage = "", order = "", imageUrl = "", mobileLink = "trustwallet://", playStore = "")
private val safe: Wallet
    get() = Wallet(id = "3", name = "Safe", homePage = "", order = "", imageUrl = "", mobileLink = "safe://", playStore = "")
private val rainbow: Wallet
    get() = Wallet(id = "4", name = "Rainbow", homePage = "", order = "", imageUrl = "", mobileLink = "rainbow://", playStore = "")
private val zerion: Wallet
    get() = Wallet(id = "5", name = "Zerion", homePage = "", order = "", imageUrl = "", mobileLink = "zerion://", playStore = "")
private val argent: Wallet
    get() = Wallet(id = "6", name = "Argent", homePage = "", order = "", imageUrl = "", mobileLink = "argent://", playStore = "")
private val spot: Wallet
    get() = Wallet(id = "7", name = "Spot", homePage = "", order = "", imageUrl = "", mobileLink = "spot://", playStore = "")
private val imToken: Wallet
    get() = Wallet(id = "8", name = "imToken", homePage = "", order = "", imageUrl = "", mobileLink = "imtoken://", playStore = "")
private val alphaWallet: Wallet
    get() = Wallet(id = "9", name = "AlphaWallet", homePage = "", order = "", imageUrl = "", mobileLink = "alphawallet://", playStore = "")
private val omni: Wallet
    get() = Wallet(id = "10", name = "Omni", homePage = "", order = "", imageUrl = "", mobileLink = "omni://", playStore = "")
private val bitkeep: Wallet
    get() = Wallet(id = "11", name = "BitKeep", homePage = "", order = "", imageUrl = "", mobileLink = "bitkeep://", playStore = "")
private val tokenPocket: Wallet
    get() = Wallet(id = "12", name = "TokePocket", homePage = "", order = "", imageUrl = "", mobileLink = "tokenpocket://", playStore = "")
private val ledgerLive: Wallet
    get() = Wallet(id = "13", name = "Ledger Live", homePage = "", order = "", imageUrl = "", mobileLink = "ledgerlive://", playStore = "")
private val frontier: Wallet
    get() = Wallet(id = "14", name = "Frontier", homePage = "", order = "", imageUrl = "", mobileLink = "frontier://", playStore = "")
private val safePal: Wallet
    get() = Wallet(id = "15", name = "SafePal", homePage = "", order = "",imageUrl = "", mobileLink = "safepal://", playStore = "")

internal val testWallets: List<Wallet>
    get() = listOf(metaMask, trustWallet, safe, rainbow, zerion, argent, spot, imToken, alphaWallet, omni, bitkeep, tokenPocket, ledgerLive, frontier, safePal)

internal val accountDataPreview: AccountData
    get() = AccountData(
    topic = "",
    address = "0xd2B8b483056b134f9D8cd41F55bB065F9",
    balance = "543 ETH",
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

private val ethToken = Modal.Model.Token(name = "Ether", symbol = "ETH", decimal = 18,)

internal val ethereumChain: Modal.Model.Chain
    get() = Modal.Model.Chain(
        chainName = "Ethereum",
        chainNamespace = "eip155",
        chainReference = "1",
        methods = listOf(),
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
        methods = listOf(),
        events = listOf(),
        token = ethToken,
        rpcUrl = "https://arb1.arbitrum.io/rpc",
        blockExplorerUrl = "https://arbiscan.io"
    )

internal val testChains: List<Modal.Model.Chain>
    get() = listOf(ethereumChain, arbitrumChain)
