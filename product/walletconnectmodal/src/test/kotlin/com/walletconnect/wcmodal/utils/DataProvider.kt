package com.walletconnect.wcmodal.utils

import com.walletconnect.android.internal.common.modal.data.model.Wallet

private val metaMask: Wallet
    get() = Wallet(id = "1", name = "MetaMask", homePage = "", order = "", imageUrl = "", mobileLink = "metamask://", webAppLink = "", playStore = "")
private val trustWallet: Wallet
    get() = Wallet(id = "2", name = "Trust Wallet", homePage = "", order = "", imageUrl = "", mobileLink = "trustwallet://", webAppLink = "",playStore = "")
private val safe: Wallet
    get() = Wallet(id = "3", name = "Safe", homePage = "", order = "", imageUrl = "", mobileLink = "safe://", webAppLink = "", playStore = "")
private val rainbow: Wallet
    get() = Wallet(id = "4", name = "Rainbow", homePage = "", order = "", imageUrl = "", mobileLink = "rainbow://", webAppLink = "", playStore = "")
private val zerion: Wallet
    get() = Wallet(id = "5", name = "Zerion", homePage = "", order = "", imageUrl = "", mobileLink = "zerion://", webAppLink = "", playStore = "")
private val argent: Wallet
    get() = Wallet(id = "6", name = "Argent", homePage = "", order = "", imageUrl = "", mobileLink = "argent://", webAppLink = "", playStore = "")
private val spot: Wallet
    get() = Wallet(id = "7", name = "Spot", homePage = "", order = "", imageUrl = "", mobileLink = "spot://", webAppLink = "", playStore = "")
private val imToken: Wallet
    get() = Wallet(id = "8", name = "imToken", homePage = "", order = "", imageUrl = "", mobileLink = "imtoken://", webAppLink = "", playStore = "")
private val alphaWallet: Wallet
    get() = Wallet(id = "9", name = "AlphaWallet", homePage = "", order = "", imageUrl = "", mobileLink = "alphawallet://", webAppLink = "", playStore = "")
private val omni: Wallet
    get() = Wallet(id = "10", name = "Omni", homePage = "", order = "", imageUrl = "", mobileLink = "omni://", webAppLink = "", playStore = "")
private val bitkeep: Wallet
    get() = Wallet(id = "11", name = "BitKeep", homePage = "", order = "", imageUrl = "", mobileLink = "bitkeep://", webAppLink = "", playStore = "")
private val tokenPocket: Wallet
    get() = Wallet(id = "12", name = "TokePocket", homePage = "", order = "", imageUrl = "", mobileLink = "tokenpocket://", webAppLink = "", playStore = "")
private val ledgerLive: Wallet
    get() = Wallet(id = "13", name = "Ledger Live", homePage = "", order = "", imageUrl = "", mobileLink = "ledgerlive://", webAppLink = "", playStore = "")
private val frontier: Wallet
    get() = Wallet(id = "14", name = "Frontier", homePage = "", order = "", imageUrl = "", mobileLink = "frontier://", webAppLink = "", playStore = "")
private val safePal: Wallet
    get() = Wallet(id = "15", name = "SafePal", homePage = "", order = "",imageUrl = "", mobileLink = "safepal://", webAppLink = "", playStore = "")

internal val testWallets: List<Wallet>
    get() = listOf(metaMask, trustWallet, safe, rainbow, zerion, argent, spot, imToken, alphaWallet, omni, bitkeep, tokenPocket, ledgerLive, frontier, safePal)
