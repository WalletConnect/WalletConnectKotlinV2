package com.walletconnect.walletconnectv2.core.exceptions.peer

import com.walletconnect.walletconnectv2.core.exceptions.DISCONNECT_MESSAGE

internal sealed class PeerReason {
    abstract val message: String
    abstract val code: Int


    object UserDisconnected : PeerError() {
        override val message: String = DISCONNECT_MESSAGE
        override val code: Int = 6000
    }
}