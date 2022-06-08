package com.walletconnect.walletconnectv2.engine.model.mapper

import com.walletconnect.walletconnectv2.core.exceptions.peer.PeerError
import com.walletconnect.walletconnectv2.engine.model.ValidationError

@JvmSynthetic
internal fun ValidationError.toPeerError() = when (this) {
    is ValidationError.UnsupportedNamespaceKey -> PeerError.UnsupportedNamespaceKey(message)
    is ValidationError.UnsupportedChains -> PeerError.UnsupportedChains(message)
    is ValidationError.InvalidEvent -> PeerError.InvalidEvent(message)
    is ValidationError.InvalidExtendRequest -> PeerError.InvalidExtendRequest(message)
    is ValidationError.InvalidSessionRequest -> PeerError.InvalidMethod(message)
    is ValidationError.UnauthorizedEvent -> PeerError.UnauthorizedEvent(message)
    is ValidationError.UnauthorizedMethod -> PeerError.UnauthorizedMethod(message)
    is ValidationError.UserRejected -> PeerError.UserRejected(message)
    is ValidationError.UserRejectedEvents -> PeerError.UserRejectedEvents(message)
    is ValidationError.UserRejectedMethods -> PeerError.UserRejectedMethods(message)
    is ValidationError.UserRejectedChains -> PeerError.UserRejectedChains(message)
}