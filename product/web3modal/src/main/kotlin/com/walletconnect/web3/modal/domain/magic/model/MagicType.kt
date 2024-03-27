package com.walletconnect.web3.modal.domain.magic.model

internal enum class MagicTypeResponse(val value: String) {
        SYNC_THEME_SUCCESS("@w3m-frame/SYNC_THEME_SUCCESS"),
        SYNC_DAPP_DATA_SUCCESS("@w3m-frame/SYNC_DAPP_DATA_SUCCESS"),
        CONNECT_EMAIL_SUCCESS("@w3m-frame/CONNECT_EMAIL_SUCCESS"),
        CONNECT_EMAIL_ERROR ("@w3m-frame/CONNECT_EMAIL_ERROR"),
        IS_CONNECTED_SUCCESS("@w3m-frame/IS_CONNECTED_SUCCESS"),
        IS_CONNECTED_ERROR("@w3m-frame/IS_CONNECTED_ERROR"),
        CONNECT_OTP_SUCCESS("@w3m-frame/CONNECT_OTP_SUCCESS"),
        CONNECT_OTP_ERROR("@w3m-frame/CONNECT_OTP_ERROR"),
        GET_USER_SUCCESS("@w3m-frame/GET_USER_SUCCESS"),
        GET_USER_ERROR("@w3m-frame/GET_USER_ERROR"),
        SESSION_UPDATE("@w3m-frame/SESSION_UPDATE"),
        SWITCH_NETWORK_SUCCESS("@w3m-frame/SWITCH_NETWORK_SUCCESS"),
        SWITCH_NETWORK_ERROR("@w3m-frame/SWITCH_NETWORK_ERROR"),
        RPC_REQUEST_SUCCESS("@w3m-frame/RPC_REQUEST_SUCCESS"),
        RPC_REQUEST_ERROR("@w3m-frame/RPC_REQUEST_ERROR"),
        SIGN_OUT_SUCCESS("@w3m-frame/SIGN_OUT_SUCCESS"),
}

internal enum class MagicTypeRequest(val value: String) {
        IS_CONNECTED("@w3m-app/IS_CONNECTED"),
        SWITCH_NETWORK("@w3m-app/SWITCH_NETWORK"),
        CONNECT_EMAIL("@w3m-app/CONNECT_EMAIL"),
        CONNECT_DEVICE("@w3m-app/CONNECT_DEVICE"),
        CONNECT_OTP("@w3m-app/CONNECT_OTP"),
        GET_USER("@w3m-app/GET_USER"),
        SIGN_OUT("@w3m-app/SIGN_OUT"),
        GET_CHAIN_ID("@w3m-app/GET_CHAIN_ID"),
        RPC_REQUEST("@w3m-app/RPC_REQUEST"),
        UPDATE_EMAIL("@w3m-app/UPDATE_EMAIL"),
        SYNC_THEME("@w3m-app/SYNC_THEME"),
        SYNC_DAPP_DATA("@w3m-app/SYNC_DAPP_DATA"),
}
