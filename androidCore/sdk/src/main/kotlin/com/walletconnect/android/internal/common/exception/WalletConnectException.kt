package com.walletconnect.android.internal.common.exception

abstract class WalletConnectException(override val message: String?) : Exception(message)