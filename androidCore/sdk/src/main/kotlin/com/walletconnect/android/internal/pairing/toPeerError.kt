package com.walletconnect.android.internal.pairing

import com.walletconnect.android.internal.ValidationError

@JvmSynthetic
internal fun ValidationError.toPeerError() = when (this) {
    is ValidationError.UnsupportedNamespaceKey -> PeerError.CAIP25.UnsupportedNamespaceKey(message)
    is ValidationError.UnsupportedChains -> PeerError.CAIP25.UnsupportedChains(message)
    is ValidationError.InvalidEvent -> PeerError.Invalid.Event(message)
    is ValidationError.InvalidExtendRequest -> PeerError.Invalid.ExtendRequest(message)
    is ValidationError.InvalidSessionRequest -> PeerError.Invalid.Method(message)
    is ValidationError.UnauthorizedEvent -> PeerError.Unauthorized.Event(message)
    is ValidationError.UnauthorizedMethod -> PeerError.Unauthorized.Method(message)
    is ValidationError.UserRejected -> PeerError.CAIP25.UserRejected(message)
    is ValidationError.UserRejectedEvents -> PeerError.CAIP25.UserRejectedEvents(message)
    is ValidationError.UserRejectedMethods -> PeerError.CAIP25.UserRejectedMethods(message)
    is ValidationError.UserRejectedChains -> PeerError.CAIP25.UserRejectedChains(message)
}