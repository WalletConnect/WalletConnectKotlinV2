package com.walletconnect.android.pulse.model

internal object EventType {
    @get:JvmSynthetic
    const val MODAL_CREATED: String = "MODAL_CREATED"

    @get:JvmSynthetic
    const val MODAL_LOADED: String = "MODAL_LOADED"

    @get:JvmSynthetic
    const val MODAL_OPEN: String = "MODAL_OPEN"

    @get:JvmSynthetic
    const val MODAL_CLOSE: String = "MODAL_CLOSE"

    @get:JvmSynthetic
    const val CLICK_ALL_WALLETS: String = "CLICK_ALL_WALLETS"

    @get:JvmSynthetic
    const val CLICK_NETWORKS: String = "CLICK_NETWORKS"

    @get:JvmSynthetic
    const val SWITCH_NETWORK: String = "SWITCH_NETWORK"

    @get:JvmSynthetic
    const val SELECT_WALLET: String = "SELECT_WALLET"

    @get:JvmSynthetic
    const val CONNECT_SUCCESS: String = "CONNECT_SUCCESS"

    @get:JvmSynthetic
    const val CONNECT_ERROR: String = "CONNECT_ERROR"

    @get:JvmSynthetic
    const val DISCONNECT_SUCCESS: String = "DISCONNECT_SUCCESS"

    @get:JvmSynthetic
    const val DISCONNECT_ERROR: String = "DISCONNECT_ERROR"

    @get:JvmSynthetic
    const val CLICK_WALLET_HELP: String = "CLICK_WALLET_HELP"

    @get:JvmSynthetic
    const val CLICK_NETWORK_HELP: String = "CLICK_NETWORK_HELP"

    @get:JvmSynthetic
    const val CLICK_GET_WALLET: String = "CLICK_GET_WALLET"
}