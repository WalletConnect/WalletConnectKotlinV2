package com.walletconnect.web3.inbox.client

import com.walletconnect.chat.client.Chat
import com.walletconnect.notify.client.Notify
import com.walletconnect.web3.inbox.common.model.Config

@JvmSynthetic
internal fun Inbox.Model.Cacao.Signature.toChat(): Chat.Model.Cacao.Signature = Chat.Model.Cacao.Signature(t, s, m)

@JvmSynthetic
internal fun Inbox.Model.Cacao.Signature.toNotify(): Notify.Model.Cacao.Signature = Notify.Model.Cacao.Signature(t, s, m)

@JvmSynthetic
internal fun Inbox.Model.Config.toCommon() = Config(isChatEnabled, isNotifyEnabled, areSettingsEnabled)