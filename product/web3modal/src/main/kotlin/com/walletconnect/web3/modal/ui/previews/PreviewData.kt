package com.walletconnect.web3.modal.ui.previews

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.walletconnect.android.internal.common.explorer.data.model.Wallet

private val metaMask = Wallet(id = "1", name = "MetaMask", imageUrl = "", nativeLink = "metamask://", universalLink = "", playStoreLink = "")
private val trustWallet = Wallet(id = "2", name = "Trust Wallet", imageUrl = "", nativeLink = "trustwallet://", universalLink = null, playStoreLink = "")
private val safe = Wallet(id = "3", name = "Safe", imageUrl = "", nativeLink = "safe://", universalLink = null, playStoreLink = "")
private val rainbow = Wallet(id = "4", name = "Rainbow", imageUrl = "", nativeLink = "rainbow://", universalLink = null, playStoreLink = "")
private val zerion = Wallet(id = "5", name = "Zerion", imageUrl = "", nativeLink = "zerion://", universalLink = null, playStoreLink = "")
private val argent = Wallet(id = "6", name = "Argent", imageUrl = "", nativeLink = "argent://", universalLink = null, playStoreLink = "")
private val spot = Wallet(id = "7", name = "Spot", imageUrl = "", nativeLink = "spot://", universalLink = null, playStoreLink = "")
private val imToken = Wallet(id = "8", name = "imToken", imageUrl = "", nativeLink = "imtoken://", universalLink = null, playStoreLink = "")
private val alphaWallet = Wallet(id = "9", name = "AlphaWallet", imageUrl = "", nativeLink = "alphawallet://", universalLink = null, playStoreLink = "")
private val omni = Wallet(id = "10", name = "Omni", imageUrl = "", nativeLink = "omni://", universalLink = null, playStoreLink = "")
private val bitkeep = Wallet(id = "11", name = "BitKeep", imageUrl = "", nativeLink = "bitkeep://", universalLink = null, playStoreLink = "")
private val tokenPocket = Wallet(id = "12", name = "TokePocket", imageUrl = "", nativeLink = "tokenpocket://", universalLink = null, playStoreLink = "")
private val ledgerLive = Wallet(id = "13", name = "Ledger Live", imageUrl = "", nativeLink = "ledgerlive://", universalLink = null, playStoreLink = "")
private val frontier = Wallet(id = "14", name = "Frontier", imageUrl = "", nativeLink = "frontier://", universalLink = null, playStoreLink = "")
private val safePal = Wallet(id = "15", name = "SafePal", imageUrl = "", nativeLink = "safepal://", universalLink = null, playStoreLink = "")

internal val testWallets = listOf(metaMask, trustWallet, safe, rainbow, zerion, argent, spot, imToken, alphaWallet, omni, bitkeep, tokenPocket, ledgerLive, frontier, safePal)

internal class ConnectYourWalletPreviewProvider : PreviewParameterProvider<List<Wallet>> {
    override val values = sequenceOf(
        listOf(),
        testWallets.take(3),
        testWallets.take(4),
        testWallets.take(6),
        testWallets
    )
}

