package com.walletconnect.android.pulse.model

internal object EventType {

	@get:JvmSynthetic
	const val NO_WSS_CONNECTION: String = "NO_WSS_CONNECTION"

	@get:JvmSynthetic
	const val NO_INTERNET_CONNECTION: String = "NO_INTERNET_CONNECTION"

	internal object Pairing {
		@get:JvmSynthetic
		const val MALFORMED_PAIRING_URI: String = "MALFORMED_PAIRING_URI"

		@get:JvmSynthetic
		const val PAIRING_ALREADY_EXIST: String = "PAIRING_ALREADY_EXIST"

		@get:JvmSynthetic
		const val PAIRING_SUBSCRIPTION_FAILURE: String = "FAILED_TO_SUBSCRIBE_TO_PAIRING_TOPIC"

		@get:JvmSynthetic
		const val PAIRING_EXPIRED: String = "PAIRING_EXPIRED"
	}

	internal object Session {
		@get:JvmSynthetic
		const val PROPOSAL_EXPIRED: String = "PROPOSAL_EXPIRED"

		@get:JvmSynthetic
		const val SESSION_SUBSCRIPTION_FAILURE: String = "SESSION_SUBSCRIPTION_FAILURE"

		@get:JvmSynthetic
		const val SESSION_APPROVE_PUBLISH_FAILURE: String = "SESSION_APPROVE_PUBLISH_FAILURE"

		@get:JvmSynthetic
		const val SESSION_SETTLE_PUBLISH_FAILURE: String = "SESSION_SETTLE_PUBLISH_FAILURE"

		@get:JvmSynthetic
		const val SESSION_APPROVE_NAMESPACE_VALIDATION_FAILURE: String = "SESSION_APPROVE_NAMESPACE_VALIDATION_FAILURE"
	}

	internal object SessionAuthenticate {
		@get:JvmSynthetic
		const val MISSING_SESSION_AUTH_REQUEST: String = "missing_session_authenticate_request"

		@get:JvmSynthetic
		const val SESSION_AUTH_REQUEST_EXPIRED: String = "session_authenticate_request_expired"

		@get:JvmSynthetic
		const val CHAINS_CAIP2_COMPLIANT_FAILURE: String = "chains_caip2_compliant_failure"

		@get:JvmSynthetic
		const val CHAINS_EVM_COMPLIANT_FAILURE: String = "chains_evm_compliant_failure"

		@get:JvmSynthetic
		const val INVALID_CACAO: String = "invalid_cacao"

		@get:JvmSynthetic
		const val SUBSCRIBE_AUTH_SESSION_TOPIC_FAILURE: String = "subscribe_authenticated_session_topic_failure"

		@get:JvmSynthetic
		const val AUTHENTICATED_SESSION_APPROVE_PUBLISH_FAILURE: String = "authenticated_session_approve_publish_failure"
	}

	internal object W3M {
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
}