package com.walletconnect.walletconnectv2.core.exceptions

internal const val NO_SEQUENCE_FOR_TOPIC_MESSAGE: String = "Cannot find sequence for given topic: "
internal const val PAIRING_NOW_ALLOWED_MESSAGE: String = "Pair with existing pairing is not allowed"
internal const val UNAUTHORIZED_UPDATE_MESSAGE: String =
    "The update() was called by the unauthorized peer. Must be called by controller client."
internal const val UNAUTHORIZED_EXTEND_MESSAGE: String =
    "The extend() was called by the unauthorized peer. Must be called by controller client."
internal const val UNAUTHORIZED_EMIT_MESSAGE: String =
    "The emit() was called by the unauthorized peer. Must be called by controller client."
internal const val SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE: String = "Session is not acknowledged, topic: "

internal const val NAMESPACE_CHAINS_MISSING_MESSAGE: String = "Chains must not be empty"
internal const val NAMESPACE_CHAINS_CAIP_2_MESSAGE: String = "Chains must be CAIP-2 compliant"
internal const val NAMESPACE_CHAINS_WRONG_NAMESPACE_MESSAGE: String = "Chains must be defined in matching namespace"
internal const val NAMESPACE_EXTENSION_CHAINS_MISSING_MESSAGE: String = "Extension chains must not be empty"
internal const val NAMESPACE_KEYS_CAIP_2_MESSAGE: String = "Namespace formatting must match CAIP-2"

internal const val NAMESPACE_MISSING_PROPOSAL_MESSAGE: String = "No proposal for Session Namespace"
internal const val NAMESPACE_ACCOUNTS_MISSING_MESSAGE: String = "Accounts must not be empty"
internal const val NAMESPACE_ACCOUNTS_CAIP_10_MESSAGE: String = "Accounts must be CAIP-10 compliant"
internal const val NAMESPACE_METHODS_MISSING_MESSAGE: String = "All methods must be approved"
internal const val NAMESPACE_EVENTS_MISSING_MESSAGE: String = "All events must be approved"
internal const val NAMESPACE_ACCOUNTS_MISSING_FOR_CHAINS_MESSAGE: String = "All chains must have at least one account"
internal const val NAMESPACE_ACCOUNTS_WRONG_NAMESPACE_MESSAGE: String = "Accounts must be defined in matching namespace"
internal const val NAMESPACE_EXTENSION_ACCOUNTS_MISSING_MESSAGE: String = "Extension accounts must not be empty"
internal const val NAMESPACE_KEYS_MISSING_MESSAGE: String = "All namespaces must be approved"

internal const val UNAUTHORIZED_METHOD_MESSAGE: String = "Unauthorized method is not authorized for given chain"
internal const val UNAUTHORIZED_EVENT_MESSAGE: String = "Unauthorized event is not authorized for given chain"
internal const val INVALID_EVENT_MESSAGE: String = "Event name and data fields cannot be empty. ChainId must be CAIP-2 compliant"
internal const val INVALID_REQUEST_MESSAGE: String = "Request topic, method and params fields cannot be empty. ChainId must be CAIP-2 compliant"
internal const val MALFORMED_PAIRING_URI_MESSAGE: String = "Pairing URI string is invalid."
internal const val INVALID_EXTEND_TIME: String = "Extend time is out of range"
internal const val NO_SESSION_PROPOSAL: String = "No session proposal for proposer publicKey: "

internal const val WRONG_CONNECTION_TYPE: String = "Wrong connection type. Please, choose manual connection on initialisation."
internal const val DISCONNECT_MESSAGE: String = "User disconnected"