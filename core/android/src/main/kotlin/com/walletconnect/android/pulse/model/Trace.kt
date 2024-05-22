package com.walletconnect.android.pulse.model

object Trace {
    object Pairing {
        const val PAIRING_STARTED = "pairing_started"
        const val PAIRING_URI_VALIDATION_SUCCESS = "pairing_uri_validation_success"
        const val PAIRING_URI_NOT_EXPIRED = "pairing_uri_not_expired"
        const val STORE_NEW_PAIRING = "store_new_pairing"
        const val EXISTING_PAIRING = "existing_pairing"
        const val PAIRING_NOT_EXPIRED = "pairing_not_expired"
        const val EMIT_INACTIVE_PAIRING = "emit_inactive_pairing"
        const val EMIT_SESSION_PROPOSAL = "emit_session_proposal"
        const val SUBSCRIBING_PAIRING_TOPIC = "subscribing_pairing_topic"
        const val SUBSCRIBE_PAIRING_TOPIC_SUCCESS = "subscribe_pairing_topic_success"
    }

    object Session {
        const val SESSION_APPROVE_STARTED = "session_approve_started"
        const val PROPOSAL_NOT_EXPIRED = "proposal_not_expired"
        const val SESSION_NAMESPACE_VALIDATION_SUCCESS = "session_namespaces_validation_success"
        const val CREATE_SESSION_TOPIC = "create_session_topic"
        const val SUBSCRIBING_SESSION_TOPIC = "subscribing_session_topic"
        const val SUBSCRIBE_SESSION_TOPIC_SUCCESS = "subscribe_session_topic_success"
        const val PUBLISHING_SESSION_APPROVE = "publishing_session_approve"
        const val SESSION_APPROVE_PUBLISH_SUCCESS = "session_approve_publish_success"
        const val STORE_SESSION = "store_session"
        const val PUBLISHING_SESSION_SETTLE = "publishing_session_settle"
        const val SESSION_SETTLE_PUBLISH_SUCCESS = "session_settle_publish_success"
    }

    object SessionAuthenticate {
        const val SESSION_AUTHENTICATE_APPROVE_STARTED = "authenticated_session_approve_started"
        const val AUTHENTICATED_SESSION_NOT_EXPIRED = "authenticated_session_not_expired"
        const val CHAINS_CAIP2_COMPLIANT = "chains_caip2_compliant"
        const val CHAINS_EVM_COMPLIANT = "chains_evm_compliant"
        const val CREATE_AUTHENTICATED_SESSION_TOPIC = "create_authenticated_session_topic"
        const val CACAOS_VERIFIED = "cacaos_verified"
        const val STORE_AUTHENTICATED_SESSION = "store_authenticated_session"
        const val SUBSCRIBING_AUTHENTICATED_SESSION_TOPIC = "subscribing_authenticated_session_topic"
        const val SUBSCRIBE_AUTHENTICATED_SESSION_TOPIC_SUCCESS = "subscribe_authenticated_session_topic_success"
        const val PUBLISHING_AUTHENTICATED_SESSION_APPROVE = "publishing_authenticated_session_approve"
        const val AUTHENTICATED_SESSION_APPROVE_PUBLISH_SUCCESS = "authenticated_session_approve_publish_success"
    }
}