package com.walletconnect.web3.modal.client.models

import com.walletconnect.android.internal.common.exception.WalletConnectException

@Deprecated("com.walletconnect.web3.modal.client.models.Web3ModelClientAlreadyInitializedException has been deprecated. Please use com.reown.appkit.client.models.Web3ModelClientAlreadyInitializedException instead from - https://github.com/reown-com/reown-kotlin")
class Web3ModelClientAlreadyInitializedException : WalletConnectException("Web3Modal already initialized")

@Deprecated("com.walletconnect.web3.modal.client.models.CoinbaseClientAlreadyInitializedException has been deprecated. Please use com.reown.appkit.client.models.CoinbaseClientAlreadyInitializedException instead from - https://github.com/reown-com/reown-kotlin")
class CoinbaseClientAlreadyInitializedException : WalletConnectException("Coinbase already initialized")