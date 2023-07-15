package com.walletconnect.web3.inbox.client

import com.walletconnect.chat.client.Chat
import com.walletconnect.push.common.Push
import com.walletconnect.web3.inbox.common.model.Config

@JvmSynthetic
internal fun Inbox.Model.Cacao.Signature.toChat(): Chat.Model.Cacao.Signature = Chat.Model.Cacao.Signature(t, s, m)

@JvmSynthetic
internal fun Inbox.Model.Cacao.Signature.toPush(): Push.Model.Cacao.Signature = Push.Model.Cacao.Signature(t, s, m)

@JvmSynthetic
internal fun Inbox.Model.Config.toCommon() = Config(isChatEnabled, isPushEnabled, areSettingsEnabled)