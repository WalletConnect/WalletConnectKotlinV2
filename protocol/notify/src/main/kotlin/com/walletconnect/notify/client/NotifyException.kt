package com.walletconnect.notify.client

import com.walletconnect.android.internal.common.exception.WalletConnectException

class InvalidDidJsonFileException(override val message: String?) : WalletConnectException(message)