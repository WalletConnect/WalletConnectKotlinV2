package com.walletconnect.android.internal.common.model

import com.walletconnect.android.internal.common.exception.WalletConnectException

class UnknownEnvelopeTypeException(override val message: String?) : WalletConnectException(message)
class MissingParticipantsException(override val message: String?) : WalletConnectException(message)
class MissingKeyException(override val message: String?) : WalletConnectException(message)