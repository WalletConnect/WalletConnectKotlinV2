package com.walletconnect.android.pulse.model.properties

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.pulse.model.EventType

sealed class Props {
    abstract val event: String
    abstract val type: String

    @JsonClass(generateAdapter = true)
    data class ModalCreated(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.MODAL_CREATED,
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class ModalLoaded(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.MODAL_LOADED,
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class ModalOpen(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.MODAL_OPEN,
        @Json(name = "properties")
        val properties: ModalConnectedProperties
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class ModalClose(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.MODAL_CLOSE,
        @Json(name = "properties")
        val properties: ModalConnectedProperties
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class ClickNetworks(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.CLICK_NETWORKS,
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class ClickAllWallets(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.CLICK_ALL_WALLETS,
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class SwitchNetwork(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.SWITCH_NETWORK,
        @Json(name = "properties")
        val properties: NetworkProperties
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class SelectWallet(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.SELECT_WALLET,
        @Json(name = "properties")
        val properties: SelectWalletProperties
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class ConnectSuccess(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.CONNECT_SUCCESS,
        @Json(name = "properties")
        val properties: ConnectSuccessProperties
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class ConnectError(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.CONNECT_ERROR,
        @Json(name = "properties")
        val properties: ConnectErrorProperties
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class DisconnectSuccess(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.DISCONNECT_SUCCESS
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class DisconnectError(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.DISCONNECT_ERROR
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class ClickWalletHelp(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.CLICK_WALLET_HELP
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class ClickNetworkHelp(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.CLICK_NETWORK_HELP
    ) : Props()

    @JsonClass(generateAdapter = true)
    data class ClickGetWallet(
        @Json(name = "event")
        override val event: String = "track",
        @Json(name = "type")
        override val type: String = EventType.CLICK_GET_WALLET
    ) : Props()
}
