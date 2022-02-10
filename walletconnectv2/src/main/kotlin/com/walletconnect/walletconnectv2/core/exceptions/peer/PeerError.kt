package com.walletconnect.walletconnectv2.core.exceptions.peer

enum class Error(val message: String, val code: Int) {
    INVALID_SESSION_UPDATE("Invalid session update request", 1003),
    INVALID_PAIRING_UPDATE("Invalid pairing update request", 1003),
    INVALID_SESSION_UPGRADE("Invalid session upgrade request", 1004),
    NO_MATCHING_SESSION_TOPIC("No matching session with topic: ", 1301),
    NO_MATCHING_PAIRING_TOPIC("No matching pairing with topic: ", 1301),

    //3000 (Unauthorized)
    UNAUTHORIZED_TARGET_CHAIN_ID("Unauthorized Target ChainId Requested: ", 3000),
    UNAUTHORIZED_JSON_RPC_METHOD("Unauthorized JSON-RPC Method Requested: ", 3001),
    UNAUTHORIZED_NOTIFICATION_TYPE("Unauthorized Notification Type Requested: ", 3002),
    UNAUTHORIZED_SESSION_UPDATE("Unauthorized session update message", 3003),
    UNAUTHORIZED_PAIRING_UPDATE("Unauthorized pairing update message", 3003),
    UNAUTHORIZED_SESSION_UPGRADE("Unauthorized session update message", 3004),
    UNAUTHORIZED_MATCHING_CONTROLLER("Unauthorized: peer is also ", 3005),

    //    5000 (CAIP-25)
    DISAPPROVE_REQUESTED_CHAINS("User disapproved requested chains", 5000),
    DISAPPROVE_REQUESTED_JSON_RPC("User disapproved requested json-rpc methods", 5001),
    DISAPPROVE_NOTIFICATION_TYPES("User disapproved requested notification types", 5002),
    NOT_SUPPORTED_CHAINS("Requested chains are not supported: ", 5100),
    NOT_SUPPORTED_JSON_RPC_METHODS("Requested json-rpc methods are not supported: ", 5101),
    NOT_SUPPORTED_NOTIFICATION_TYPES("Requested notification types are not supported: ", 5102),
    NOT_SUPPORTED_SIGNAL("Proposed session signal is unsupported", 5103),
    USER_DISCONNECTED("User disconnected session", 5900),
}