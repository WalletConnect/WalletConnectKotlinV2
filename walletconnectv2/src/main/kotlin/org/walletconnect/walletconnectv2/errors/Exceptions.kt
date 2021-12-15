package org.walletconnect.walletconnectv2.errors

sealed class WalletConnectExceptions(override val message: String?) : Exception(message) {
    class ApiKeyDoesNotExistException(override val message: String?) : WalletConnectExceptions(message)
    class InvalidApiKeyException(override val message: String?) : WalletConnectExceptions(message)
    class ServerException(override val message: String?) : WalletConnectExceptions(message)
}