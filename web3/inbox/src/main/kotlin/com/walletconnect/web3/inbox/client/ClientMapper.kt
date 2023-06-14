package com.walletconnect.web3.inbox.client

import com.walletconnect.chat.client.Chat

@JvmSynthetic
internal fun Inbox.Model.Cacao.Signature.toChat(): Chat.Model.Cacao.Signature = Chat.Model.Cacao.Signature(t, s, m)