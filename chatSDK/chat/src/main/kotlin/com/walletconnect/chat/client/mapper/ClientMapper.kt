@file:JvmSynthetic

package com.walletconnect.chat.client.mapper

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.engine.model.EngineDO

//TODO: Provide VO objects for engine classes. Remove using the EngineDO object in the client layer
internal fun Chat.Params.Invite.toInviteEngineDO(): EngineDO.Invite {
    return EngineDO.Invite(invite.account.value, invite.message, invite.signature)
}

internal fun Chat.Params.Message.toMessageEngineDO() : EngineDO.SendMessage {
    return EngineDO.SendMessage(message, media.toMediaEngineDO())
}

private fun Chat.Model.Media.toMediaEngineDO() : EngineDO.Media {
    return EngineDO.Media(type, data)
}

