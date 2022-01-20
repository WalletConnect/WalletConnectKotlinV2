package com.walletconnect.walletconnectv2.common.errors

sealed class WalletConnectExceptions(override val message: String?) : Exception(message) {
    class ProjectIdDoesNotExistException(override val message: String?) : WalletConnectExceptions(message)
    class InvalidProjectIdException(override val message: String?) : WalletConnectExceptions(message)
    class ServerException(override val message: String?) : WalletConnectExceptions(message)
    class NoSequenceForTopicException(override val message: String?) : WalletConnectExceptions(message)
    class UnauthorizedFunctionCallException(override val message: String?) :
        WalletConnectExceptions("Unauthorized function call, $message method should be called by the controller")

    object PairWithExistingPairingIsNotAllowed : WalletConnectExceptions("Pair with existing pairing is not allowed")
}