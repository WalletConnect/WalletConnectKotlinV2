package com.walletconnect.chat.client

interface ChatInterface {

    interface ChatDelegate {
        fun onInvite(onInvite: Chat.Model.Events.OnInvite)
        fun onInviteAccepted(onInviteAccepted: Chat.Model.Events.OnInviteAccepted)
        fun onInviteRejected(onInviteRejected: Chat.Model.Events.OnInviteRejected)
        fun onMessage(onMessage: Chat.Model.Events.OnMessage)
        fun onLeft(onLeft: Chat.Model.Events.OnLeft)
        fun onConnectionStateChange(state: Chat.Model.ConnectionState)
        fun onError(error: Chat.Model.Error)
    }

    fun setChatDelegate(delegate: ChatDelegate)

    fun initialize(init: Chat.Params.Init, onError: (Chat.Model.Error) -> Unit)
    fun register(register: Chat.Params.Register, listener: Chat.Listeners.Register)
    fun unregister(unregister: Chat.Params.Unregister, listener: Chat.Listeners.Unregister)
    fun resolve(resolve: Chat.Params.Resolve, listener: Chat.Listeners.Resolve)
    fun goPrivate(goPrivate: Chat.Params.GoPrivate, onSuccess: () -> Unit, onError: (Chat.Model.Error) -> Unit)
    fun goPublic(goPublic: Chat.Params.GoPublic, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit)
    fun invite(invite: Chat.Params.Invite, onSuccess: (Long) -> Unit, onError: (Chat.Model.Error) -> Unit)
    fun accept(accept: Chat.Params.Accept, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit)
    fun reject(reject: Chat.Params.Reject, onSuccess: () -> Unit,  onError: (Chat.Model.Error) -> Unit)
    fun message(message: Chat.Params.Message, onSuccess: () -> Unit, onError: (Chat.Model.Error) -> Unit)
    fun ping(ping: Chat.Params.Ping, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit)
    fun leave(leave: Chat.Params.Leave, onError: (Chat.Model.Error) -> Unit)
    fun setContact(setContact: Chat.Params.SetContact, onError: (Chat.Model.Error) -> Unit)
    fun getReceivedInvites(getReceivedInvites: Chat.Params.GetReceivedInvites): Map<Long, Chat.Model.Invite.Received>
    fun getSentInvites(getSentInvites: Chat.Params.GetSentInvites): Map<Long, Chat.Model.Invite.Sent>
    fun getThreads(getThreads: Chat.Params.GetThreads): Map<String, Chat.Model.Thread>
    fun getMessages(getMessages: Chat.Params.GetMessages): List<Chat.Model.Message>
}