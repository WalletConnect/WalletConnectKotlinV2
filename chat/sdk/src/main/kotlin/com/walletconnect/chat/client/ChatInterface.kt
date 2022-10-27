package com.walletconnect.chat.client

interface ChatInterface {

    interface ChatDelegate {
        fun onInvite(onInvite: Chat.Model.Events.OnInvite)
        fun onJoined(onJoined: Chat.Model.Events.OnJoined)
        fun onMessage(onMessage: Chat.Model.Events.OnMessage)
        fun onLeft(onLeft: Chat.Model.Events.OnLeft)
        fun onError(error: Chat.Model.Error)
    }

    fun setChatDelegate(delegate: ChatDelegate)

    fun initialize(init: Chat.Params.Init, onError: (Chat.Model.Error) -> Unit)
    fun register(register: Chat.Params.Register, listener: Chat.Listeners.Register)
    fun resolve(resolve: Chat.Params.Resolve, listener: Chat.Listeners.Resolve)
    fun invite(invite: Chat.Params.Invite, onError: (Chat.Model.Error) -> Unit)
    fun accept(accept: Chat.Params.Accept, onError: (Chat.Model.Error) -> Unit)
    fun reject(reject: Chat.Params.Reject, onError: (Chat.Model.Error) -> Unit)
    fun message(message: Chat.Params.Message, onError: (Chat.Model.Error) -> Unit)
    fun ping(ping: Chat.Params.Ping, onError: (Chat.Model.Error) -> Unit)
    fun leave(leave: Chat.Params.Leave, onError: (Chat.Model.Error) -> Unit)
    fun addContact(addContact: Chat.Params.AddContact, onError: (Chat.Model.Error) -> Unit)
    fun getInvites(getInvites: Chat.Params.GetInvites): Map<String, Chat.Model.Invite>
    fun getThreads(getThreads: Chat.Params.GetThreads): Map<String, Chat.Model.Thread>
    fun getMessages(getMessages: Chat.Params.GetMessages): List<Chat.Model.Message>
}