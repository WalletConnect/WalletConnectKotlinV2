package com.walletconnect.chat.common.exceptions

import com.walletconnect.android.internal.common.model.Error

sealed class PeerError : Error {

    //TODO: Discuss error with team on later stage of development
    data class UserRejectedInvitation(override val message: String) : PeerError() {
        override val code: Int = 4001
    }
}
