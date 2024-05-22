package com.walletconnect.android.pulse.model.properties

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.pulse.model.EventType

sealed class Props {
	abstract val event: String
	abstract val type: String

	@JsonClass(generateAdapter = true)
	data class NoWSSConnection(
		@Json(name = "event")
		override val event: String = "error",
		@Json(name = "type")
		override val type: String = EventType.Pairing.NO_WSS_CONNECTION
	) : Props()

	@JsonClass(generateAdapter = true)
	data class NoInternetConnection(
		@Json(name = "event")
		override val event: String = "error",
		@Json(name = "type")
		override val type: String = EventType.Pairing.NO_INTERNET_CONNECTION

	) : Props()

	sealed class Pairing : Props() {
		@JsonClass(generateAdapter = true)
		data class MalformedPairingUri(
			@Json(name = "event")
			override val event: String = "error",
			@Json(name = "type")
			override val type: String = EventType.Pairing.MALFORMED_PAIRING_URI
		) : Props()

		@JsonClass(generateAdapter = true)
		data class PairingAlreadyExist(
			@Json(name = "event")
			override val event: String = "error",
			@Json(name = "type")
			override val type: String = EventType.Pairing.PAIRING_ALREADY_EXIST
		) : Props()

		@JsonClass(generateAdapter = true)
		data class PairingSubscriptionFailure(
			@Json(name = "event")
			override val event: String = "error",
			@Json(name = "type")
			override val type: String = EventType.Pairing.PAIRING_SUBSCRIPTION_FAILURE
		) : Props()

		@JsonClass(generateAdapter = true)
		data class PairingExpired(
			@Json(name = "event")
			override val event: String = "error",
			@Json(name = "type")
			override val type: String = EventType.Pairing.PAIRING_EXPIRED
		) : Props()
	}

	sealed class Sign : Props() {
		@JsonClass(generateAdapter = true)
		data class ProposalExpired(
			@Json(name = "event")
			override val event: String = "error",
			@Json(name = "type")
			override val type: String = EventType.Session.PROPOSAL_EXPIRED
		) : Props()

		@JsonClass(generateAdapter = true)
		data class SessionSubscriptionFailure(
			@Json(name = "event")
			override val event: String = "error",
			@Json(name = "type")
			override val type: String = EventType.Session.SESSION_SUBSCRIPTION_FAILURE
		) : Props()

		@JsonClass(generateAdapter = true)
		data class SessionApprovePublishFailure(
			@Json(name = "event")
			override val event: String = "error",
			@Json(name = "type")
			override val type: String = EventType.Session.SESSION_APPROVE_PUBLISH_FAILURE
		) : Props()

		@JsonClass(generateAdapter = true)
		data class SessionSettlePublishFailure(
			@Json(name = "event")
			override val event: String = "error",
			@Json(name = "type")
			override val type: String = EventType.Session.SESSION_SETTLE_PUBLISH_FAILURE
		) : Props()

		@JsonClass(generateAdapter = true)
		data class SessionApproveNamespaceValidationFailure(
			@Json(name = "event")
			override val event: String = "error",
			@Json(name = "type")
			override val type: String = EventType.Session.SESSION_APPROVE_NAMESPACE_VALIDATION_FAILURE
		) : Props()
	}

	sealed class W3M : Props() {
		@JsonClass(generateAdapter = true)
		data class ModalCreated(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.MODAL_CREATED,
		) : Props()

		@JsonClass(generateAdapter = true)
		data class ModalLoaded(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.MODAL_LOADED,
		) : Props()

		@JsonClass(generateAdapter = true)
		data class ModalOpen(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.MODAL_OPEN,
			@Json(name = "properties")
			val properties: ModalConnectedProperties
		) : Props()

		@JsonClass(generateAdapter = true)
		data class ModalClose(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.MODAL_CLOSE,
			@Json(name = "properties")
			val properties: ModalConnectedProperties
		) : Props()

		@JsonClass(generateAdapter = true)
		data class ClickNetworks(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.CLICK_NETWORKS,
		) : Props()

		@JsonClass(generateAdapter = true)
		data class ClickAllWallets(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.CLICK_ALL_WALLETS,
		) : Props()

		@JsonClass(generateAdapter = true)
		data class SwitchNetwork(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.SWITCH_NETWORK,
			@Json(name = "properties")
			val properties: NetworkProperties
		) : Props()

		@JsonClass(generateAdapter = true)
		data class SelectWallet(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.SELECT_WALLET,
			@Json(name = "properties")
			val properties: SelectWalletProperties
		) : Props()

		@JsonClass(generateAdapter = true)
		data class ConnectSuccess(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.CONNECT_SUCCESS,
			@Json(name = "properties")
			val properties: ConnectSuccessProperties
		) : Props()

		@JsonClass(generateAdapter = true)
		data class ConnectError(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.CONNECT_ERROR,
			@Json(name = "properties")
			val properties: ConnectErrorProperties
		) : Props()

		@JsonClass(generateAdapter = true)
		data class DisconnectSuccess(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.DISCONNECT_SUCCESS
		) : Props()

		@JsonClass(generateAdapter = true)
		data class DisconnectError(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.DISCONNECT_ERROR
		) : Props()

		@JsonClass(generateAdapter = true)
		data class ClickWalletHelp(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.CLICK_WALLET_HELP
		) : Props()

		@JsonClass(generateAdapter = true)
		data class ClickNetworkHelp(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.CLICK_NETWORK_HELP
		) : Props()

		@JsonClass(generateAdapter = true)
		data class ClickGetWallet(
			@Json(name = "event")
			override val event: String = "track",
			@Json(name = "type")
			override val type: String = EventType.W3M.CLICK_GET_WALLET
		) : Props()
	}
}
