package org.walletconnect.walletconnectv2.errors

class ApiKeyDoesNotExistException(override val message: String?) : Exception(message)
class InvalidApiKeyException(override val message: String?) : Exception(message)
class ServerException(override val message: String?) : Exception(message)
class NoSessionProposalException : Exception()
class NoSessionRequestPayloadException : Exception()
class NoSessionDeletePayloadException : Exception()
class ApplicationCannotBeNullException : Exception()
class UnExpectedError : Exception()