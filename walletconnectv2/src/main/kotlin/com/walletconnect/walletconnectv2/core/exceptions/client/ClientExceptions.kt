package com.walletconnect.walletconnectv2.core.exceptions.client

sealed class WalletConnectException(override val message: String?) : Exception(message) {
    class ProjectIdDoesNotExistException(override val message: String?) : WalletConnectException(message)
    class InvalidProjectIdException(override val message: String?) : WalletConnectException(message)
    class GenericException(override val message: String?) : WalletConnectException(message)

    class MalformedWalletConnectUri(override val message: String?) : WalletConnectException(message)
    class UnauthorizedPeerException(override val message: String?) : WalletConnectException(message)
    class InvalidSessionPermissionsException(override val message: String?) : WalletConnectException(message)
    class InvalidSessionProposalException(override val message: String?) : WalletConnectException(message)
    class InvalidAccountsException(override val message: String?) : WalletConnectException(message)
    class InvalidNotificationException(override val message: String?) : WalletConnectException(message)
    class UnauthorizedNotificationException(override val message: String?) : WalletConnectException(message)
    class UnauthorizedChainIdException(override val message: String?) : WalletConnectException(message)
    class NotSettledSessionException(override val message: String?) : WalletConnectException(message)

    class CannotFindSequenceForTopic(override val message: String?) : WalletConnectException(message)
    class PairWithExistingPairingIsNotAllowed(override val message: String?) : WalletConnectException(message)

    class InvalidExtendException(override val message: String?) : WalletConnectException(message)
}