package com.walletconnect.android.exception

import com.walletconnect.android.common.exception.WalletConnectException

class GenericException(override val message: String?) : WalletConnectException(message)
class InternalError(override val message: String?): WalletConnectException(message)

class MalformedWalletConnectUri(override val message: String?) : WalletConnectException(message)
class PairWithExistingPairingIsNotAllowed(override val message: String?) : WalletConnectException(message)
class CannotFindSequenceForTopic(override val message: String?) : WalletConnectException(message)

class InvalidProjectIdException(override val message: String?) : WalletConnectException(message)
class ProjectIdDoesNotExistException(override val message: String?) : WalletConnectException(message)
class NoRelayConnectionException(override val message: String?) : WalletConnectException(message)