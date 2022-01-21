package com.walletconnect.walletconnectv2.common.errors

internal sealed class WalletConnectExceptions(override val message: String?) : Exception(message) {
    internal class ProjectIdDoesNotExistException(override val message: String?) : WalletConnectExceptions(message)
    internal class InvalidProjectIdException(override val message: String?) : WalletConnectExceptions(message)
    internal class ServerException(override val message: String?) : WalletConnectExceptions(message)
    internal class NoSequenceForTopicException(override val message: String?) : WalletConnectExceptions(message)
    internal object PairWithExistingPairingIsNotAllowed : WalletConnectExceptions("Pair with existing pairing is not allowed")
}