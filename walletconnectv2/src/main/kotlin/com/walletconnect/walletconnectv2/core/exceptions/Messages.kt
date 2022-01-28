package com.walletconnect.walletconnectv2.core.exceptions

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