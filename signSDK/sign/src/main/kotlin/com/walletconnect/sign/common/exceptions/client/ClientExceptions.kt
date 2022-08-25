package com.walletconnect.sign.common.exceptions.client

import com.walletconnect.android_core.common.WalletConnectException

class UnauthorizedPeerException(override val message: String?) : WalletConnectException(message)
class UnauthorizedMethodException(override val message: String?) : WalletConnectException(message)
class UnauthorizedEventException(override val message: String?) : WalletConnectException(message)

class InvalidNamespaceException(override val message: String?) : WalletConnectException(message)
class InvalidEventException(override val message: String?) : WalletConnectException(message)
class InvalidRequestException(override val message: String?) : WalletConnectException(message)

class NotSettledSessionException(override val message: String?) : WalletConnectException(message)
class CannotFindSequenceForTopic(override val message: String?) : WalletConnectException(message)
class CannotFindSessionProposalException(override val message: String?) : WalletConnectException(message)