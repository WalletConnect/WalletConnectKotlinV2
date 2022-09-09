package com.walletconnect.android.api.exception

open class WalletConnectException(override val message: String?) : Exception(message)

class GenericException(override val message: String?) : WalletConnectException(message)
class InternalError(override val message: String?): WalletConnectException(message)

class InvalidProjectIdException(override val message: String?) : WalletConnectException(message)
class ProjectIdDoesNotExistException(override val message: String?) : WalletConnectException(message)
class NoRelayConnectionException(override val message: String?) : WalletConnectException(message)