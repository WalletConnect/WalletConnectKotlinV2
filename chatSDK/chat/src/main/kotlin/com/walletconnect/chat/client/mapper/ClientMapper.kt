@file:JvmSynthetic

package com.walletconnect.chat.client.mapper

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.core.model.vo.AccountIdVO
import com.walletconnect.chat.core.model.vo.MediaVO
import com.walletconnect.chat.engine.model.EngineDO

//TODO: Figure out what to do with models separation
@JvmSynthetic
internal fun Chat.Params.Invite.toEngineDO(): EngineDO.Invite {
    return EngineDO.Invite(invite.account.toVO(), invite.message, invite.signature)
}

@JvmSynthetic
internal fun Chat.Params.Message.toEngineDO() : EngineDO.SendMessage {
    return EngineDO.SendMessage(message, media?.toVO())
}

@JvmSynthetic
private fun Chat.Model.Media.toVO() : MediaVO {
    return MediaVO(type, data)
}

@JvmSynthetic
internal fun Chat.Model.AccountId.toVO(): AccountIdVO {
    return AccountIdVO(value)
}

@JvmSynthetic
internal fun EngineDO.Events.OnInvite.toClient(): Chat.Model.Events.OnInvite {
    return Chat.Model.Events.OnInvite(id, invite.toClient())
}

@JvmSynthetic
internal fun EngineDO.Invite.toClient(): Chat.Model.Invite {
    return Chat.Model.Invite(accountId.toClient(), message, signature)
}

@JvmSynthetic
internal fun AccountIdVO.toClient(): Chat.Model.AccountId {
    return Chat.Model.AccountId(value)
}

@JvmSynthetic
internal fun MediaVO.toClient(): Chat.Model.Media {
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
internal fun EngineDO.Message.toClient(): Chat.Model.Message {
    return Chat.Model.Message(message, authorAccountId.toClient(), timestamp, media?.toClient())
}

