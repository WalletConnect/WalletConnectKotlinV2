package com.walletconnect.android.api

open class WalletConnectException(override val message: String?) : Exception(message)

class InternalError(override val message: String?): WalletConnectException(message)