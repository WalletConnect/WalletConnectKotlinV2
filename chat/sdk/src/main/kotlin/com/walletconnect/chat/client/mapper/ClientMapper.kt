@file:JvmSynthetic

package com.walletconnect.chat.client.mapper

import com.walletconnect.android.impl.common.model.ConnectionState
import com.walletconnect.android.impl.common.SDKError
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.common.model.Media
import com.walletconnect.chat.engine.model.EngineDO

//TODO: Figure out what to do with models separation
@JvmSynthetic
internal fun Chat.Params.Invite.toEngineDO(): EngineDO.Invite {
    return EngineDO.Invite(invite.account.toCommon(), invite.message, invite.publicKey, invite.signature)
}

@JvmSynthetic
internal fun Chat.Params.Message.toEngineDO(): EngineDO.SendMessage {
    return EngineDO.SendMessage(author.toCommon(), message, media?.toCommon())
}

@JvmSynthetic
private fun Chat.Model.Media.toCommon(): Media {
    return Media(type, data)
}

@JvmSynthetic
internal fun Chat.Model.AccountId.toCommon(): AccountId {
    return AccountId(value)
}

@JvmSynthetic
internal fun EngineDO.Events.OnInvite.toClient(): Chat.Model.Events.OnInvite {
    return Chat.Model.Events.OnInvite(id, invite.toClient())
}

@JvmSynthetic
internal fun EngineDO.Invite.toClient(): Chat.Model.Invite {
    return Chat.Model.Invite(accountId.toClient(), message, publicKey, signature)
}

@JvmSynthetic
internal fun AccountId.toClient(): Chat.Model.AccountId {
    return Chat.Model.AccountId(value)
}

@JvmSynthetic
internal fun Media.toClient(): Chat.Model.Media {
    return Chat.Model.Media(type, data)
}

@JvmSynthetic
internal fun EngineDO.Events.OnJoined.toClient(): Chat.Model.Events.OnJoined {
    return Chat.Model.Events.OnJoined(topic)
}

@JvmSynthetic
internal fun EngineDO.Events.OnMessage.toClient(): Chat.Model.Events.OnMessage {
    return Chat.Model.Events.OnMessage(topic, message.toClient())
}

@JvmSynthetic
internal fun EngineDO.Events.OnLeft.toClient(): Chat.Model.Events.OnLeft {
    return Chat.Model.Events.OnLeft(topic)
}

@JvmSynthetic
internal fun EngineDO.Events.OnReject.toClient(): Chat.Model.Events.OnReject {
    return Chat.Model.Events.OnReject(topic)
}

@JvmSynthetic
internal fun EngineDO.Message.toClient(): Chat.Model.Message {
    return Chat.Model.Message(message, authorAccountId.toClient(), timestamp, media?.toClient())
}

@JvmSynthetic
internal fun SDKError.toClientError(): Chat.Model.Error =
    Chat.Model.Error(this.exception)

@JvmSynthetic
internal fun ConnectionState.toClient(): Chat.Model.ConnectionState =
    Chat.Model.ConnectionState(isAvailable)