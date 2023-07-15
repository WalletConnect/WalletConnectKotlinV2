@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

internal sealed interface Invite {
    val id: Long
    val inviterAccount: AccountId
    val inviteeAccount: AccountId
    val message: InviteMessage
    val inviterPublicKey: PublicKey
    val status: InviteStatus
    val acceptTopic: Topic
    val symmetricKey: SymmetricKey
    val inviterPrivateKey: PrivateKey?
    val timestamp: Long

    data class Received(
        override val id: Long,
        override val inviterAccount: AccountId,
        override val inviteeAccount: AccountId,
        override val message: InviteMessage,
        override val inviterPublicKey: PublicKey,
        override val status: InviteStatus,
        override val acceptTopic: Topic,
        override val symmetricKey: SymmetricKey,
        override val inviterPrivateKey: PrivateKey?,
        override val timestamp: Long,
    ) : Invite

    data class Sent(
        override val id: Long,
        override val inviterAccount: AccountId,
        override val inviteeAccount: AccountId,
        override val message: InviteMessage,
        override val inviterPublicKey: PublicKey,
        override val status: InviteStatus,
        override val acceptTopic: Topic,
        override val symmetricKey: SymmetricKey,
        override val inviterPrivateKey: PrivateKey?,
        override val timestamp: Long,
    ) : Invite
}