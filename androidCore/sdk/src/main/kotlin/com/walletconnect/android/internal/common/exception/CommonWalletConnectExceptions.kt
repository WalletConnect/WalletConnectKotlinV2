package com.walletconnect.android.internal.common.exception

class GenericException(override val message: String?) : WalletConnectException(message)

class MalformedWalletConnectUri(override val message: String?) : WalletConnectException(message)
class PairWithExistingPairingIsNotAllowed(override val message: String?) : WalletConnectException(message)
class CannotFindSequenceForTopic(override val message: String?) : WalletConnectException(message)

class InvalidProjectIdException(override val message: String?) : WalletConnectException(message)
class ProjectIdDoesNotExistException(override val message: String?) : WalletConnectException(message)
class NoRelayConnectionException(override val message: String?) : WalletConnectException(message)
class CannotFindKeyPairException(override val message: String?) : WalletConnectException(message)