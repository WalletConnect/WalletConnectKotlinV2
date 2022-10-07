package com.walletconnect.android.internal

internal sealed class ValidationError(val message: String) {

    //Namespaces validation
    object UnsupportedNamespaceKey : ValidationError(NAMESPACE_KEYS_CAIP_2_MESSAGE)
    class UnsupportedChains(_message: String) : ValidationError(_message)

    //Rejected errors
    object UserRejected : ValidationError(NAMESPACE_KEYS_MISSING_MESSAGE)
    class UserRejectedChains(_message: String) : ValidationError(_message)
    object UserRejectedMethods : ValidationError(NAMESPACE_METHODS_MISSING_MESSAGE)
    object UserRejectedEvents : ValidationError(NAMESPACE_EVENTS_MISSING_MESSAGE)

    //Authorization errors
    object UnauthorizedMethod : ValidationError(UNAUTHORIZED_METHOD_MESSAGE)
    object UnauthorizedEvent : ValidationError(UNAUTHORIZED_EVENT_MESSAGE)

    //Validation errors
    object InvalidSessionRequest : ValidationError(INVALID_REQUEST_MESSAGE)
    object InvalidEvent : ValidationError(INVALID_EVENT_MESSAGE)
    object InvalidExtendRequest : ValidationError(INVALID_EXTEND_TIME)
}
