package com.walletconnect.auth.common.exceptions

import com.walletconnect.android_core.common.WalletConnectException

class CannotFindSequenceForTopic(override val message: String?) : WalletConnectException(message)
