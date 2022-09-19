package com.walletconnect.android.impl.common

import com.walletconnect.android.exception.WalletConnectException

class UnknownEnvelopeTypeException(override val message: String?) : WalletConnectException(message)
class MissingParticipantsException(override val message: String?) : WalletConnectException(message)
class MissingReceiverPublicKeyException(override val message: String?) : WalletConnectException(message)

class MalformedWalletConnectUri(override val message: String?) : WalletConnectException(message)
class PairWithExistingPairingIsNotAllowed(override val message: String?) : WalletConnectException(message)