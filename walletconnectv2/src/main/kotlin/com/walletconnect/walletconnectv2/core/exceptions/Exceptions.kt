package com.walletconnect.walletconnectv2.core.exceptions

sealed class WalletConnectException(override val message: String?) : Exception(message) {
    class ProjectIdDoesNotExistException(override val message: String?) : WalletConnectException(message)
    class InvalidProjectIdException(override val message: String?) : WalletConnectException(message)
    class ServerException(override val message: String?) : WalletConnectException(message)
    class CannotFindSequenceForTopic(val topic: String) : WalletConnectException("Cannot find sequence for given topic: $topic")
    object PairWithExistingPairingIsNotAllowed : WalletConnectException("Pair with existing pairing is not allowed")
    class UnauthorizedPeerException(override val message: String?) : WalletConnectException(message)
}