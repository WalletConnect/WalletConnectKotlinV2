package com.walletconnect.web3.modal.utils

@Deprecated("com.walletconnect.web3.modal.utils.EthUtils has been deprecated. Please use com.reown.appkit.utils.EthUtils instead from - https://github.com/reown-com/reown-kotlin")
object EthUtils {
    const val walletSwitchEthChain = "wallet_switchEthereumChain"
    const val walletAddEthChain = "wallet_addEthereumChain"

    val ethRequiredMethods = listOf(
        "personal_sign",
        "eth_signTypedData",
        "eth_sendTransaction",
    )


    val ethOptionalMethods = listOf(walletSwitchEthChain, walletAddEthChain)

    val ethMethods = ethRequiredMethods + ethOptionalMethods

    const val chainChanged = "chainChanged"
    const val accountsChanged = "accountsChanged"

    val ethEvents = listOf(chainChanged, accountsChanged)
}
