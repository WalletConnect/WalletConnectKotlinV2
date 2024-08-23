package com.walletconnect.android.internal.common.exception

class GenericException(override val message: String?) : WalletConnectException(message)

class MalformedWalletConnectUri(override val message: String?) : WalletConnectException(message)
class PairWithExistingPairingIsNotAllowed(override val message: String?) : WalletConnectException(message)
class ExpiredPairingException(override val message: String?) : WalletConnectException(message)
class ExpiredPairingURIException(override val message: String?) : WalletConnectException(message)
class CannotFindSequenceForTopic(override val message: String?) : WalletConnectException(message)

class InvalidProjectIdException(override val message: String?) : WalletConnectException(message)
class UnableToConnectToWebsocketException(override val message: String?) : WalletConnectException(message)
class ProjectIdDoesNotExistException(override val message: String?) : WalletConnectException(message)
open class NoConnectivityException(override val message: String?) : WalletConnectException(message)
class NoInternetConnectionException(override val message: String?) : NoConnectivityException(message)
class NoRelayConnectionException(override val message: String?) : NoConnectivityException(message)
class CannotFindKeyPairException(override val message: String?) : WalletConnectException(message)
class InvalidExpiryException(override val message: String? = "Request expiry validation failed. Expiry must be between current timestamp + MIN_INTERVAL and current timestamp + MAX_INTERVAL (MIN_INTERVAL: 300, MAX_INTERVAL: 604800)") :
    WalletConnectException(message)

class RequestExpiredException(override val message: String?) : WalletConnectException(message)