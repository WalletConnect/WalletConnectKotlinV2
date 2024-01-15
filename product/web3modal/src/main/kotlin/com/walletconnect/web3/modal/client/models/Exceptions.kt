package com.walletconnect.web3.modal.client.models

import com.walletconnect.android.internal.common.exception.WalletConnectException

class Web3ModelClientAlreadyInitializedException : WalletConnectException("Web3Modal already initialized")
class CoinbaseClientAlreadyInitializedException : WalletConnectException("Coinbase already initialized")