package com.walletconnect.chat.client

interface ChatInterface {

    interface ChatDelegate {
        fun onInvite(onInvite: Chat.Model.Events.OnInvite)
        fun onJoined(onJoined: Chat.Model.Events.OnJoined)
        fun onReject(onReject: Chat.Model.Events.OnReject)
        fun onMessage(onMessage: Chat.Model.Events.OnMessage)
        fun onLeft(onLeft: Chat.Model.Events.OnLeft)
        fun onConnectionStateChange(state: Chat.Model.ConnectionState)
        fun onError(error: Chat.Model.Error)
    }

    fun setChatDelegate(delegate: ChatDelegate)

    fun initialize(init: Chat.Params.Init, onError: (Chat.Model.Error) -> Unit)
    fun resolve(resolve: Chat.Params.Resolve, listener: Chat.Listeners.Resolve)
    fun invite(invite: Chat.Params.Invite, onError: (Chat.Model.Error) -> Unit)
    fun accept(accept: Chat.Params.Accept, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit)
    fun reject(reject: Chat.Params.Reject, onError: (Chat.Model.Error) -> Unit)
    fun message(message: Chat.Params.Message, onError: (Chat.Model.Error) -> Unit)
    fun ping(ping: Chat.Params.Ping, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit)
    fun leave(leave: Chat.Params.Leave, onError: (Chat.Model.Error) -> Unit)
    fun addContact(addContact: Chat.Params.AddContact, onError: (Chat.Model.Error) -> Unit)
    fun getInvites(getInvites: Chat.Params.GetInvites): Map<String, Chat.Model.Invite>
    fun getThreads(getThreads: Chat.Params.GetThreads): Map<String, Chat.Model.Thread>
    fun getMessages(getMessages: Chat.Params.GetMessages): List<Chat.Model.Message>

    // TODO: Conform to Chat API https://github.com/WalletConnect/WalletConnectKotlinV2/issues/599
    fun registerIdentity(registerIdentity: Chat.Params.RegisterIdentity, listener: Chat.Listeners.RegisterIdentity)
    fun registerInvite(register: Chat.Params.Register, listener: Chat.Listeners.Register)
}