package com.walletconnect.web3.modal.ui.previews

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.model.Chain
import com.walletconnect.web3.modal.domain.model.Identity

private val metaMask: Wallet
    get() = Wallet(id = "1", name = "MetaMask", imageUrl = "", nativeLink = "metamask://", universalLink = "", playStoreLink = "")
private val trustWallet: Wallet
    get() = Wallet(id = "2", name = "Trust Wallet", imageUrl = "", nativeLink = "trustwallet://", universalLink = null, playStoreLink = "")
private val safe: Wallet
    get() = Wallet(id = "3", name = "Safe", imageUrl = "", nativeLink = "safe://", universalLink = null, playStoreLink = "")
private val rainbow: Wallet
    get() = Wallet(id = "4", name = "Rainbow", imageUrl = "", nativeLink = "rainbow://", universalLink = null, playStoreLink = "")
private val zerion: Wallet
    get() = Wallet(id = "5", name = "Zerion", imageUrl = "", nativeLink = "zerion://", universalLink = null, playStoreLink = "")
private val argent: Wallet
    get() = Wallet(id = "6", name = "Argent", imageUrl = "", nativeLink = "argent://", universalLink = null, playStoreLink = "")
private val spot: Wallet
    get() = Wallet(id = "7", name = "Spot", imageUrl = "", nativeLink = "spot://", universalLink = null, playStoreLink = "")
private val imToken: Wallet
    get() = Wallet(id = "8", name = "imToken", imageUrl = "", nativeLink = "imtoken://", universalLink = null, playStoreLink = "")
private val alphaWallet: Wallet
    get() = Wallet(id = "9", name = "AlphaWallet", imageUrl = "", nativeLink = "alphawallet://", universalLink = null, playStoreLink = "")
private val omni: Wallet
    get() = Wallet(id = "10", name = "Omni", imageUrl = "", nativeLink = "omni://", universalLink = null, playStoreLink = "")
private val bitkeep: Wallet
    get() = Wallet(id = "11", name = "BitKeep", imageUrl = "", nativeLink = "bitkeep://", universalLink = null, playStoreLink = "")
private val tokenPocket: Wallet
    get() = Wallet(id = "12", name = "TokePocket", imageUrl = "", nativeLink = "tokenpocket://", universalLink = null, playStoreLink = "")
private val ledgerLive: Wallet
    get() = Wallet(id = "13", name = "Ledger Live", imageUrl = "", nativeLink = "ledgerlive://", universalLink = null, playStoreLink = "")
private val frontier: Wallet
    get() = Wallet(id = "14", name = "Frontier", imageUrl = "", nativeLink = "frontier://", universalLink = null, playStoreLink = "")
private val safePal: Wallet
    get() = Wallet(id = "15", name = "SafePal", imageUrl = "", nativeLink = "safepal://", universalLink = null, playStoreLink = "")

internal val testWallets: List<Wallet>
    get() = listOf(metaMask, trustWallet, safe, rainbow, zerion, argent, spot, imToken, alphaWallet, omni, bitkeep, tokenPocket, ledgerLive, frontier, safePal)

internal val accountDataPreview: AccountData
    get() = AccountData(
    topic = "",
    address = "0xd2B8b483056b134f9D8cd41F55bB065F9",
    balance = "543 ETH",
    selectedChain = Chain("eip155:1"),
    chains = listOf(Chain("eip155:1")),
    identity = Identity()
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

