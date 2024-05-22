package com.walletconnect.android.pulse.model.properties

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.pulse.model.EventType


sealed class Props {
    abstract val event: String
    abstract val type: String

    sealed class Error : Props() {
        abstract val properties: TraceProperties?

        @JsonClass(generateAdapter = true)
        data class NoWSSConnection(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.NO_WSS_CONNECTION,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class NoInternetConnection(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.NO_INTERNET_CONNECTION,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class MalformedPairingUri(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.MALFORMED_PAIRING_URI,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class PairingAlreadyExist(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.PAIRING_ALREADY_EXIST,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class PairingSubscriptionFailure(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.PAIRING_SUBSCRIPTION_FAILURE,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class PairingExpired(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.PAIRING_EXPIRED,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()


        @JsonClass(generateAdapter = true)
        data class ProposalExpired(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.PROPOSAL_EXPIRED,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class SessionSubscriptionFailure(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.SESSION_SUBSCRIPTION_FAILURE,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class SessionApprovePublishFailure(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.SESSION_APPROVE_PUBLISH_FAILURE,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class SessionSettlePublishFailure(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.SESSION_SETTLE_PUBLISH_FAILURE,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class SessionApproveNamespaceValidationFailure(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.SESSION_APPROVE_NAMESPACE_VALIDATION_FAILURE,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class RequiredNamespaceValidationFailure(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.REQUIRED_NAMESPACE_VALIDATION_FAILURE,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class OptionalNamespaceValidationFailure(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.OPTIONAL_NAMESPACE_VALIDATION_FAILURE,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class SessionPropertiesValidationFailure(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.SESSION_PROPERTIES_VALIDATION_FAILURE,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()


        @JsonClass(generateAdapter = true)
        data class MissingSessionAuthenticateRequest(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.MISSING_SESSION_AUTH_REQUEST,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class SessionAuthenticateRequestExpired(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.SESSION_AUTH_REQUEST_EXPIRED,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class ChainsCaip2CompliantFailure(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.CHAINS_CAIP2_COMPLIANT_FAILURE,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class ChainsEvmCompliantFailure(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.CHAINS_EVM_COMPLIANT_FAILURE,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class InvalidCacao(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.INVALID_CACAO,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class SubscribeAuthenticatedSessionTopicFailure(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.SUBSCRIBE_AUTH_SESSION_TOPIC_FAILURE,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class AuthenticatedSessionApprovePublishFailure(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.AUTHENTICATED_SESSION_APPROVE_PUBLISH_FAILURE,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()

        @JsonClass(generateAdapter = true)
        data class AuthenticatedSessionExpired(
            @Json(name = "event")
            override val event: String = "error",
            @Json(name = "type")
            override val type: String = EventType.Error.AUTHENTICATED_SESSION_EXPIRED,
            @Json(name = "properties")
            override val properties: TraceProperties? = null
        ) : Error()
    }

    sealed class Track : Props() {
        @JsonClass(generateAdapter = true)
        data class ModalCreated(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.MODAL_CREATED,
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class ModalLoaded(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.MODAL_LOADED,
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class ModalOpen(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.MODAL_OPEN,
            @Json(name = "properties")
            val properties: ModalConnectedProperties
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class ModalClose(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.MODAL_CLOSE,
            @Json(name = "properties")
            val properties: ModalConnectedProperties
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class ClickNetworks(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.CLICK_NETWORKS,
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class ClickAllWallets(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.CLICK_ALL_WALLETS,
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class SwitchNetwork(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.SWITCH_NETWORK,
            @Json(name = "properties")
            val properties: NetworkProperties
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class SelectWallet(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.SELECT_WALLET,
            @Json(name = "properties")
            val properties: SelectWalletProperties
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class ConnectSuccess(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.CONNECT_SUCCESS,
            @Json(name = "properties")
            val properties: ConnectSuccessProperties
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class ConnectError(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.CONNECT_ERROR,
            @Json(name = "properties")
            val properties: ConnectErrorProperties
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class DisconnectSuccess(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.DISCONNECT_SUCCESS
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class DisconnectError(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.DISCONNECT_ERROR
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class ClickWalletHelp(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.CLICK_WALLET_HELP
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class ClickNetworkHelp(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.CLICK_NETWORK_HELP
        ) : Track()

        @JsonClass(generateAdapter = true)
        data class ClickGetWallet(
            @Json(name = "event")
            override val event: String = "track",
            @Json(name = "type")
            override val type: String = EventType.Track.CLICK_GET_WALLET
        ) : Track()
    }
}
