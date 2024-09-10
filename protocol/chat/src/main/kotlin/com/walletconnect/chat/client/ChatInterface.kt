package com.walletconnect.chat.client

@Deprecated("ChatSDK has been deprecated")
interface ChatInterface {

    @Deprecated("ChatSDK has been deprecated")
    interface ChatDelegate {
        @Deprecated("ChatSDK has been deprecated")
        fun onInvite(onInvite: Chat.Model.Events.OnInvite)
        @Deprecated("ChatSDK has been deprecated")
        fun onInviteAccepted(onInviteAccepted: Chat.Model.Events.OnInviteAccepted)
        @Deprecated("ChatSDK has been deprecated")
        fun onInviteRejected(onInviteRejected: Chat.Model.Events.OnInviteRejected)
        @Deprecated("ChatSDK has been deprecated")
        fun onMessage(onMessage: Chat.Model.Events.OnMessage)
        @Deprecated("ChatSDK has been deprecated")
        fun onLeft(onLeft: Chat.Model.Events.OnLeft)
        @Deprecated("ChatSDK has been deprecated")
        fun onConnectionStateChange(state: Chat.Model.ConnectionState)
        @Deprecated("ChatSDK has been deprecated")
        fun onError(error: Chat.Model.Error)
    }

    @Deprecated("ChatSDK has been deprecated")
    fun setChatDelegate(delegate: ChatDelegate)

    @Deprecated("ChatSDK has been deprecated")
    fun initialize(init: Chat.Params.Init, onError: (Chat.Model.Error) -> Unit)
    @Deprecated("ChatSDK has been deprecated")
    fun register(register: Chat.Params.Register, listener: Chat.Listeners.Register)
    @Deprecated("ChatSDK has been deprecated")
    fun unregister(unregister: Chat.Params.Unregister, listener: Chat.Listeners.Unregister)
    @Deprecated("ChatSDK has been deprecated")
    fun resolve(resolve: Chat.Params.Resolve, listener: Chat.Listeners.Resolve)
    @Deprecated("ChatSDK has been deprecated")
    fun goPrivate(goPrivate: Chat.Params.GoPrivate, onSuccess: () -> Unit, onError: (Chat.Model.Error) -> Unit)
    @Deprecated("ChatSDK has been deprecated")
    fun goPublic(goPublic: Chat.Params.GoPublic, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit)
    @Deprecated("ChatSDK has been deprecated")
    fun invite(invite: Chat.Params.Invite, onSuccess: (Long) -> Unit, onError: (Chat.Model.Error) -> Unit)
    @Deprecated("ChatSDK has been deprecated")
    fun accept(accept: Chat.Params.Accept, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit)
    @Deprecated("ChatSDK has been deprecated")
    fun reject(reject: Chat.Params.Reject, onSuccess: () -> Unit,  onError: (Chat.Model.Error) -> Unit)
    @Deprecated("ChatSDK has been deprecated")
    fun message(message: Chat.Params.Message, onSuccess: () -> Unit, onError: (Chat.Model.Error) -> Unit)
    @Deprecated("ChatSDK has been deprecated")
    fun ping(ping: Chat.Params.Ping, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit)
    @Deprecated("ChatSDK has been deprecated")
    fun leave(leave: Chat.Params.Leave, onError: (Chat.Model.Error) -> Unit)
    @Deprecated("ChatSDK has been deprecated")
    fun getReceivedInvites(getReceivedInvites: Chat.Params.GetReceivedInvites): Map<Long, Chat.Model.Invite.Received>
    @Deprecated("ChatSDK has been deprecated")
    fun getSentInvites(getSentInvites: Chat.Params.GetSentInvites): Map<Long, Chat.Model.Invite.Sent>
    @Deprecated("ChatSDK has been deprecated")
    fun getThreads(getThreads: Chat.Params.GetThreads): Map<String, Chat.Model.Thread>
    @Deprecated("ChatSDK has been deprecated")
    fun getMessages(getMessages: Chat.Params.GetMessages): List<Chat.Model.Message>
}