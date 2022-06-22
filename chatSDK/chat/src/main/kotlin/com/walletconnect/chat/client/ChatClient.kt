package com.walletconnect.chat.client

object ChatClient: ChatInterface by ChatProtocol.instance {
    interface ChatDelegate: ChatInterface.ChatDelegate
}