package com.walletconnect.sign.core.exceptions.client

sealed class WalletConnectException(override val message: String?) : Exception(message) {
    class GenericException(override val message: String?) : WalletConnectException(message)

    class UnauthorizedPeerException(override val message: String?) : WalletConnectException(message)
    class UnauthorizedMethodException(override val message: String?) : WalletConnectException(message)
    class UnauthorizedEventException(override val message: String?) : WalletConnectException(message)

    class InvalidNamespaceException(override val message: String?) : WalletConnectException(message)
    class InvalidProjectIdException(override val message: String?) : WalletConnectException(message)
    class InvalidEventException(override val message: String?) : WalletConnectException(message)
    class InvalidRequestException(override val message: String?) : WalletConnectException(message)

    class ProjectIdDoesNotExistException(override val message: String?) : WalletConnectException(message)
    class MalformedWalletConnectUri(override val message: String?) : WalletConnectException(message)
    class NotSettledSessionException(override val message: String?) : WalletConnectException(message)
    class CannotFindSequenceForTopic(override val message: String?) : WalletConnectException(message)
    class PairWithExistingPairingIsNotAllowed(override val message: String?) : WalletConnectException(message)
    class CannotFindSessionProposalException(override val message: String?) : WalletConnectException(message)
    class MissingInternetConnectionException(override val message: String?) : WalletConnectException(message)
}