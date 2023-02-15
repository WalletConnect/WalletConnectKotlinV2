@file:JvmSynthetic

package com.walletconnect.chat.client.mapper

import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.common.model.*
import com.walletconnect.foundation.common.model.Topic

@JvmSynthetic
internal fun Chat.Params.Invite.toCommon(): SendInvite = SendInvite(inviterAccount.toCommon(), inviteeAccount.toCommon(), message.toCommon(), inviteePublicKey)

@JvmSynthetic
internal fun Chat.Params.Message.toCommon(): SendMessage = SendMessage(Topic(topic), message.toCommon(), media?.toCommon())

@JvmSynthetic
internal fun Chat.Model.Media.toCommon(): Media = Media(type, data.toCommon())

@JvmSynthetic
internal fun Events.OnInvite.toClient(): Chat.Model.Events.OnInvite = Chat.Model.Events.OnInvite(invite.toClient())


@JvmSynthetic
internal fun Thread.toClient(): Chat.Model.Thread = Chat.Model.Thread(topic, Chat.Type.AccountId(selfAccount), Chat.Type.AccountId(peerAccount))

@JvmSynthetic
internal fun InviteStatus.toClient(): Chat.Type.InviteStatus = when (this) {
    InviteStatus.PENDING -> Chat.Type.InviteStatus.PENDING
    InviteStatus.REJECTED -> Chat.Type.InviteStatus.REJECTED
    InviteStatus.APPROVED -> Chat.Type.InviteStatus.APPROVED
}

@JvmSynthetic
internal fun Invite.Received.toClient(): Chat.Model.Invite.Received =
    Chat.Model.Invite.Received(id, inviterAccount.toClient(), inviteeAccount.toClient(), message.toClient(), inviterPublicKey, inviteePublicKey, status.toClient())

@JvmSynthetic
internal fun Invite.Sent.toClient(): Chat.Model.Invite.Sent =
    Chat.Model.Invite.Sent(id, inviterAccount.toClient(), inviteeAccount.toClient(), message.toClient(), inviterPublicKey, inviteePublicKey, status.toClient())

@JvmSynthetic
internal fun AccountId.toClient(): Chat.Type.AccountId = Chat.Type.AccountId(value)

@JvmSynthetic
internal fun ChatMessage.toClient(): Chat.Type.ChatMessage = Chat.Type.ChatMessage(value)

@JvmSynthetic
internal fun InviteMessage.toClient(): Chat.Type.InviteMessage = Chat.Type.InviteMessage(value)

@JvmSynthetic
internal fun MediaData.toClient(): Chat.Type.MediaData = Chat.Type.MediaData(value)

@JvmSynthetic
internal fun Media.toClient(): Chat.Model.Media = Chat.Model.Media(type, data.toClient())

@JvmSynthetic
internal fun Events.OnJoined.toClient(): Chat.Model.Events.OnJoined = Chat.Model.Events.OnJoined(topic)

@JvmSynthetic
internal fun Events.OnMessage.toClient(): Chat.Model.Events.OnMessage = Chat.Model.Events.OnMessage(message.toClient())

@JvmSynthetic
internal fun Events.OnLeft.toClient(): Chat.Model.Events.OnLeft = Chat.Model.Events.OnLeft(topic)

@JvmSynthetic
internal fun Events.OnReject.toClient(): Chat.Model.Events.OnReject = Chat.Model.Events.OnReject(topic)

@JvmSynthetic
internal fun Message.toClient(): Chat.Model.Message = Chat.Model.Message(topic.value, message.toClient(), authorAccount.toClient(), timestamp, media?.toClient())

@JvmSynthetic
internal fun SDKError.toClientError(): Chat.Model.Error = Chat.Model.Error(this.exception)

@JvmSynthetic
internal fun ConnectionState.toClient(): Chat.Model.ConnectionState = Chat.Model.ConnectionState(isAvailable)

@JvmSynthetic
internal fun Chat.Model.Cacao.Signature.toCommon(): Cacao.Signature = Cacao.Signature(t, s, m)

@JvmSynthetic
internal fun Chat.Type.AccountId.toCommon(): AccountId = AccountId(value)

@JvmSynthetic
internal fun Chat.Type.ChatMessage.toCommon(): ChatMessage = ChatMessage(value)

@JvmSynthetic
internal fun Chat.Type.InviteMessage.toCommon(): InviteMessage = InviteMessage(value)

@JvmSynthetic
internal fun Chat.Type.MediaData.toCommon(): MediaData = MediaData(value)