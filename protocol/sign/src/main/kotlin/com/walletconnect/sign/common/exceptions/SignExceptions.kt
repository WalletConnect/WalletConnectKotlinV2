package com.walletconnect.sign.common.exceptions

import com.walletconnect.android.internal.common.exception.WalletConnectException

class UnauthorizedPeerException(override val message: String?) : WalletConnectException(message)
class UnauthorizedMethodException(override val message: String?) : WalletConnectException(message)
class UnauthorizedEventException(override val message: String?) : WalletConnectException(message)

class InvalidNamespaceException(override val message: String?) : WalletConnectException(message)
class InvalidPropertiesException(override val message: String?) : WalletConnectException(message)
class InvalidEventException(override val message: String?) : WalletConnectException(message)
class InvalidRequestException(override val message: String?) : WalletConnectException(message)

class NotSettledSessionException(override val message: String?) : WalletConnectException(message)

class SignClientAlreadyInitializedException : WalletConnectException(CLIENT_ALREADY_INITIALIZED)

class InvalidSignParamsType : WalletConnectException(INVALID_SIGN_PARAMS_TYPE)

class MissingSessionAuthenticateRequest : WalletConnectException(MISSING_SESSION_AUTHENTICATE_REQUEST)
class InvalidCacaoException : WalletConnectException(INVALID_CACAO_EXCEPTION)

class SessionProposalExpiredException(override val message: String?) : WalletConnectException(message)