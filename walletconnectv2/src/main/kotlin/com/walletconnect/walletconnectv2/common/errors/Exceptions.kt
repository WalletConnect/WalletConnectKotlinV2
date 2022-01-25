package com.walletconnect.walletconnectv2.common.errors

internal sealed class WalletConnectExceptions(override val message: String?) : Exception(message) {
    internal class ProjectIdDoesNotExistException(override val message: String?) : WalletConnectExceptions(message)
    internal class InvalidProjectIdException(override val message: String?) : WalletConnectExceptions(message)
    internal class ServerException(override val message: String?) : WalletConnectExceptions(message)
    internal class CannotFindSequenceForTopic(val topic: String) : WalletConnectExceptions("Cannot find sequence for given topic: $topic")
    internal object PairWithExistingPairingIsNotAllowed : WalletConnectExceptions("Pair with existing pairing is not allowed")
    internal class UnauthorizedPeerException(override val message: String?) : WalletConnectExceptions(message)
}