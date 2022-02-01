package com.walletconnect.walletconnectv2.core.exceptions

internal const val NO_SEQUENCE_FOR_TOPIC_MESSAGE: String = "Cannot find sequence for given topic: "
internal const val PAIRING_NOW_ALLOWED_MESSAGE: String = "Pair with existing pairing is not allowed"
internal const val UNAUTHORIZED_CONNECT_MESSAGE: String =
    "The connect() was called by the unauthorized peer. Initialize SDK with isController = false."
internal const val UNAUTHORIZED_PAIR_MESSAGE: String =
    "The pair() was called by the unauthorized peer. Initialize SDK with isController = true."
internal const val UNAUTHORIZED_APPROVE_MESSAGE: String =
    "The approve() was called by the unauthorized peer. Initialize SDK with isController = true."
internal const val UNAUTHORIZED_REJECT_MESSAGE: String =
    "The reject() was called by the unauthorized peer. Initialize SDK with isController = true."
internal const val UNAUTHORIZED_RESPOND_MESSAGE: String =
    "The respond() was called by the unauthorized peer. Initialize SDK with isController = true"
internal const val UNAUTHORIZED_REQUEST_MESSAGE: String =
    "The request() was called by the unauthorized peer. Initialize SDK with isController = false"
internal const val UNAUTHORIZED_UPDATE_MESSAGE: String =
    "The update() was called by the unauthorized peer. Initialize SDK with isController = true"
internal const val UNAUTHORIZED_UPGRADE_MESSAGE: String =
    "The upgrade() was called by the unauthorized peer. Initialize SDK with isController = true"

internal const val EMPTY_CHAIN_LIST_MESSAGE: String = "List of chains in session permissions cannot be empty"
internal const val EMPTY_RPC_METHODS_LIST_MESSAGE: String = "List of rpc methods in session permissions cannot be empty"
internal const val INVALID_NOTIFICATIONS_TYPES_MESSAGE: String = "Invalid notification types"
internal const val WRONG_CHAIN_ID_FORMAT_MESSAGE: String =
    "Blockchain chaiId does not follow the CAIP2 semantics. See: https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md"

internal const val PEER_IS_ALSO_NON_CONTROLLER_MESSAGE: String = "Unauthorized: peer is also non-controller"
internal const val PEER_IS_ALSO_CONTROLLER_MESSAGE: String = "Unauthorized: peer is also controller"