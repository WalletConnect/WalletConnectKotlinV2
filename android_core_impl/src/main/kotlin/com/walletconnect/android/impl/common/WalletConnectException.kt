package com.walletconnect.android.impl.common

open class WalletConnectException(override val message: String?) : Exception(message)

class GenericException(override val message: String?) : WalletConnectException(message)
class InternalError(override val message: String?): WalletConnectException(message)

class UnknownEnvelopeTypeException(override val message: String?) : WalletConnectException(message)
class MissingParticipantsException(override val message: String?) : WalletConnectException(message)
class MissingReceiverPublicKeyException(override val message: String?) : WalletConnectException(message)

class InvalidProjectIdException(override val message: String?) : WalletConnectException(message)
class ProjectIdDoesNotExistException(override val message: String?) : WalletConnectException(message)
class NoRelayConnectionException(override val message: String?) : WalletConnectException(message)

class MalformedWalletConnectUri(override val message: String?) : WalletConnectException(message)
class PairWithExistingPairingIsNotAllowed(override val message: String?) : WalletConnectException(message)