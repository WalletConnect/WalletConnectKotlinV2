package org.walletconnect.walletconnectv2.errors

class ApiKeyDoesNotExistException(override val message: String?) : Exception(message)
class InvalidApiKeyException(override val message: String?) : Exception(message)
class ServerException(override val message: String?) : Exception(message)
class CannotFindSubscriptionException(override val message: String?) : Exception(message)