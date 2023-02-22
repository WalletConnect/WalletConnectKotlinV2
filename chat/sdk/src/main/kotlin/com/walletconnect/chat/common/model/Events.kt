@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.android.internal.common.model.type.EngineEvent

internal sealed class Events : EngineEvent {
    data class OnInvite(val invite: Invite.Received) : Events()
    data class OnInviteAccepted(val topic: String) : Events()
    data class OnInviteRejected(val topic: String) : Events()
    data class OnMessage(val message: Message) : Events()
    data class OnLeft(val topic: String) : Events()
}