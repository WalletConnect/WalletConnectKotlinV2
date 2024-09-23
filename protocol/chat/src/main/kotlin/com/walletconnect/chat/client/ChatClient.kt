package com.walletconnect.chat.client
@Deprecated("ChatSDK has been deprecated")
object ChatClient: ChatInterface by ChatProtocol.instance {
    interface ChatDelegate: ChatInterface.ChatDelegate
}