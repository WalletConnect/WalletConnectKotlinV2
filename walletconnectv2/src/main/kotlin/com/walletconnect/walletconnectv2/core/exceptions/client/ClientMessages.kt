package com.walletconnect.walletconnectv2.core.exceptions.client

internal const val NO_SEQUENCE_FOR_TOPIC_MESSAGE: String = "Cannot find sequence for given topic: "
internal const val PAIRING_NOW_ALLOWED_MESSAGE: String = "Pair with existing pairing is not allowed"
internal const val UNAUTHORIZED_APPROVE_MESSAGE: String =
    "The approve() was called by the unauthorized peer. Must be called by controller client."
internal const val UNAUTHORIZED_UPDATE_MESSAGE: String =
    "The update() was called by the unauthorized peer. Must be called by controller client."
internal const val UNAUTHORIZED_EXTEND_MESSAGE: String =
    "The extend() was called by the unauthorized peer. Must be called by controller client."

internal const val SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE: String = "Session is not acknowledged, topic: "
internal const val EMPTY_CHAIN_LIST_MESSAGE: String = "List of chains in session permissions cannot be empty"
internal const val EMPTY_ACCOUNT_LIST_MESSAGE: String = "List of accounts cannot be empty"
internal const val EMPTY_RPC_METHODS_LIST_MESSAGE: String = "List of rpc methods in session permissions cannot be empty"
internal const val INVALID_EVENTS_MESSAGE: String = "Invalid event"
internal const val UNAUTHORIZED_EVENT_TYPE_MESSAGE: String = "Unauthorized event type"
internal const val WRONG_CHAIN_ID_FORMAT_MESSAGE: String =
    "Blockchain chaiId does not follow the CAIP2 semantics. See: https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md"

internal const val NAMESPACE_MISSING_CHAINS_MESSAGE: String = "Chains must not be empty"
internal const val NAMESPACE_CHAINS_CAIP_2_MESSAGE: String = "Chains must be CAIP-2 compliant"
internal const val NAMESPACE_CHAINS_WRONG_NAMESPACE_MESSAGE: String = "Chains must be defined in matching namespace"
internal const val NAMESPACE_EXTENSION_MISSING_CHAINS_MESSAGE: String = "Extension chains must not be empty"
internal const val NAMESPACE_EXTENSION_KEYS_CAIP_2_MESSAGE: String = "Namespace formatting must match CAIP-2"

internal const val NAMESPACE_MISSING_PROPOSAL_MESSAGE: String = "No proposal for Session Namespace"
internal const val NAMESPACE_MISSING_ACCOUNTS_MESSAGE: String = "Accounts must not be empty"
internal const val NAMESPACE_ACCOUNTS_CAIP_10_MESSAGE: String = "Accounts must be CAIP-10 compliant"
internal const val NAMESPACE_MISSING_METHODS_MESSAGE: String = "All methods must be approved"
internal const val NAMESPACE_MISSING_EVENTS_MESSAGE: String = "All events must be approved"
internal const val NAMESPACE_MISSING_ACCOUNTS_FOR_CHAINS_MESSAGE: String = "All chains must have at least one account"
internal const val NAMESPACE_ACCOUNTS_WRONG_NAMESPACE_MESSAGE: String = "Accounts must be defined in matching namespace"
internal const val NAMESPACE_KEYS_MISSING_MESSAGE: String = "All namespaces must be approved"

internal const val WRONG_ACCOUNT_ID_FORMAT_MESSAGE: String =
    "AccountIds does not follow the CAIP10 semantics. See: https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-10.md"
internal const val UNAUTHORIZED_CHAIN_ID_MESSAGE: String = "Unauthorized chain id"
internal const val UNAUTHORIZED_CHAIN_ID_OR_METHOD_MESSAGE: String = "Unauthorized chain id or method"
internal const val UNAUTHORIZED_CHAIN_ID_OR_EVENT_MESSAGE: String = "Unauthorized chain id or event"
internal const val INVALID_EVENT_MESSAGE: String = "Event name and data fields cannot be empty"
internal const val MALFORMED_PAIRING_URI_MESSAGE: String = "Pairing URI string is invalid."
internal const val INVALID_EXTEND_TIME: String = "Extend time is out of range"
internal const val NO_SESSION_PROPOSAL: String = "No session proposal for proposer publicKey: "
internal const val UNAUTHORIZED_METHOD: String = "Unauthorized method"
