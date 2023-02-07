package com.walletconnect.chat.common.exceptions

import com.walletconnect.android.internal.common.exception.WalletConnectException

class InvalidAccountIdException(override val message: String?) : WalletConnectException(message)
class UnableToExtractDomainException(override val message: String?) : WalletConnectException(message)
class InviteKeyNotFound(override val message: String?) : WalletConnectException(message)