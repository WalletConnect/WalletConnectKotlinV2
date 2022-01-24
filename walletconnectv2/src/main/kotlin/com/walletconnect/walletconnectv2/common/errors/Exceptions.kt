package com.walletconnect.walletconnectv2.common.errors

import com.walletconnect.walletconnectv2.common.model.type.ControllerType

internal sealed class WalletConnectExceptions(override val message: String?) : Exception(message) {
    internal class ProjectIdDoesNotExistException(override val message: String?) : WalletConnectExceptions(message)
    internal class InvalidProjectIdException(override val message: String?) : WalletConnectExceptions(message)
    internal class ServerException(override val message: String?) : WalletConnectExceptions(message)
    internal class CannotFindSequenceForTopic(val topic: String) : WalletConnectExceptions("Cannot find sequence for given topic: $topic")
    internal object PairWithExistingPairingIsNotAllowed : WalletConnectExceptions("Pair with existing pairing is not allowed")
    internal class UnauthorizedPeerException(method: String, controllerType: ControllerType) :
        WalletConnectExceptions("The $method was called by the unauthorized peer. Call it with: $controllerType")
}