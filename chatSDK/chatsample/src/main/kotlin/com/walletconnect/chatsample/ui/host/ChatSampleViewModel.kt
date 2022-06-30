package com.walletconnect.chatsample.ui.host

import androidx.lifecycle.ViewModel
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient

class ChatSampleViewModel : ViewModel() {
    val resolveAccountSet = setOf(
        "eip:100:0xFB3c4dD2a313CD947E7FE734bC7947E34DF93E26", // unregistered
        "eip:1:0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826",
        "eip:1:0x5496858C1f2f469Eb6A6D378C332e7a4E1dc1B4D",
        "eip:42:0x49d07a0e25d3d1881bfd1545bb9b12ac2eb00f12",
    )

    val registerAccountSet = setOf(
        "eip:1:0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826" to "2d573da1d2b8dbe3dcdb6ce7de47ce44b18fb8ec5ddc9d3f412ab4a718fff93c",
        "eip:1:0x5496858C1f2f469Eb6A6D378C332e7a4E1dc1B4D" to "e002642656e99d802437eb2c7fcd151dc292cc25e54447bf2cc18153b71233de",
        "eip:42:0x49d07a0e25d3d1881bfd1545bb9b12ac2eb00f12" to "386214f6925c951d10b2d590c7065ac5b8f29d94ab1dc08ed4d62d9c5c0841eb",
    )

    fun resolve(listener: Chat.Listeners.Resolve) {
        ChatClient.resolve(Chat.Params.Resolve(Chat.Model.AccountId(resolveAccountSet.random())), listener)
    }

    fun register(listener: Chat.Listeners.Register) {
        registerAccountSet.random().let { (account, _) ->
            ChatClient.register(Chat.Params.Register(Chat.Model.AccountId(account)),
                listener)
        }
    }
}