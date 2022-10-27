package com.walletconnect.chatsample.ui.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.domain.ChatDelegate
import com.walletconnect.chatsample.utils.tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

class ChatSharedViewModel : ViewModel() {
    var currentInvite: Chat.Model.Invite? = null
    var whoWasInvitedContact: String? = null
    val userNameToTopicMap: MutableMap<String, String> = mutableMapOf()

    val listOfInvites: MutableList<ChatUI> = mutableListOf()
    private val _listOfInvitesStateFlow: MutableStateFlow<List<ChatUI>> = MutableStateFlow(listOfInvites.toList())
    val listOfInvitesStateFlow: StateFlow<List<ChatUI>> = _listOfInvitesStateFlow

    val listOfThreads: MutableList<ChatUI> = mutableListOf()
    private val _listOfThreadsStateFlow: MutableStateFlow<List<ChatUI>> = MutableStateFlow(listOfThreads.toList())
    val listOfThreadsStateFlow: StateFlow<List<ChatUI>> = _listOfThreadsStateFlow

    val listOfMessages: MutableList<MessageUI> = mutableListOf()
    private val _listOfMessagesStateFlow: MutableStateFlow<List<MessageUI>> = MutableStateFlow(listOfMessages.toList())
    val listOfMessagesStateFlow: StateFlow<List<MessageUI>> = _listOfMessagesStateFlow

    fun getLastMessage(peerName: String) = listOfMessages.last { it.peerName == peerName }

    val emittedEvents: Flow<ChatSharedEvents> = ChatDelegate.wcEventModels.map { walletEvent: Chat.Model.Events ->
        Log.d(tag(this), walletEvent.toString())
        when (walletEvent) {
            is Chat.Model.Events.OnInvite -> walletEvent.toChatSharedEvents().also { onInvite(it) }
            is Chat.Model.Events.OnJoined -> walletEvent.toChatSharedEvents().also { onJoined(it) }
            is Chat.Model.Events.OnMessage -> walletEvent.toChatSharedEvents().also { onMessage(it) }
            is Chat.Model.Events.OnReject -> walletEvent.toChatSharedEvents()
            else -> ChatSharedEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    fun register(listener: Chat.Listeners.Register) {
        ChatClient.register(Chat.Params.Register(Chat.Model.AccountId(SELF_ACCOUNT)), listener)
    }

    fun invite(contact: String, openingMessage: String, afterInviteSent: () -> Unit) {
        ChatClient.resolve(Chat.Params.Resolve(Chat.Model.AccountId(contact)), object : Chat.Listeners.Resolve {
            override fun onError(error: Chat.Model.Error) {
                runBlocking(Dispatchers.Main) {
                    Log.e(tag(this), error.throwable.stackTraceToString())
                }
            }

            override fun onSuccess(publicKey: String) {
                ChatClient.addContact(Chat.Params.AddContact(Chat.Model.AccountId(contact), publicKey)) { error ->
                    Log.e(tag(this), error.throwable.stackTraceToString())
                }

                val inviteModel = Chat.Model.Invite(Chat.Model.AccountId(SELF_ACCOUNT), openingMessage)
                Chat.Params.Invite(Chat.Model.AccountId(contact), inviteModel).also { invite ->
                    ChatClient.invite(invite) { error -> Log.e(tag(this), error.throwable.stackTraceToString()) }
                }
                runBlocking(Dispatchers.Main) {
                    currentInvite = inviteModel
                    whoWasInvitedContact = contact
                    Log.e(tag(this), "invite: $currentInvite")
                    listOfMessages.add(MessageUI(contact, inviteModel.message, System.currentTimeMillis(), contact))
                    _listOfMessagesStateFlow.value = listOfMessages.toList()
                    afterInviteSent()
                }
            }
        })
    }

    fun acceptInvitation(chatUI: ChatUI) {
        chatUI.id?.let { id ->
            ChatClient.accept(Chat.Params.Accept(id), onSuccess = { threadTopic -> updateThread(threadTopic) }, { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            })
            updateInvites(chatUI.id)
        } ?: Log.e(tag(this), "Unable to find id on accpet invitation: $chatUI")
    }

    fun rejectInvitation(id: Long?) {
        id?.let {
            val reject = Chat.Params.Reject(it)
            ChatClient.reject(reject) { error -> Log.e(tag(this), "Unable to find reject invitation: $error") }
            updateInvites(id)
        } ?: Log.e(tag(this), "Unable to find id on reject invitation: $id")
    }

    fun sendMessage(message: String, peerName: String) {
        Log.e(tag(this), "sendMessage: $peerName")

        val (userName, topic) = userNameToTopicMap.entries.single { entry -> entry.key == peerName }
        listOfMessages.add(MessageUI(userName, message, System.currentTimeMillis(), SELF_ACCOUNT))
        _listOfMessagesStateFlow.value = listOfMessages.toList()
        ChatClient.message(Chat.Params.Message(topic, Chat.Model.AccountId(SELF_ACCOUNT), message)) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }

    private fun onInvite(event: ChatSharedEvents.OnInvite) {
        Log.d(tag(this), "Invited: ${event.invite.message}")
        val contact = event.invite.account.value
        currentInvite = event.invite

        listOfInvites.add(ChatUI(R.drawable.ic_chat_icon_3, contact, event.invite.message, event.id))
        _listOfInvitesStateFlow.update { listOfInvites.toList() }

        listOfMessages.add(MessageUI(contact, event.invite.message, System.currentTimeMillis(), contact))
        _listOfMessagesStateFlow.value = listOfMessages.toList()
    }

    private fun onJoined(event: ChatSharedEvents.OnJoined) {
        Log.d(tag(this), "Joined: ${event.topic}")
        updateThread(event.topic)
    }

    private fun onMessage(event: ChatSharedEvents.OnMessage) {
        Log.d(tag(this), "Message: ${event.message.message}")

        userNameToTopicMap.entries.find { it.value == event.topic }?.let { (_, topic) ->
            listOfThreads.find { topic == event.topic }?.let {
                val author = event.message.authorAccount.value
                listOfMessages.add(MessageUI(author, event.message.message, event.message.timestamp, author))
                _listOfMessagesStateFlow.value = listOfMessages.toList()
            } ?: Log.e(tag(this), "Unable to find topic: ${event.topic}")
        }
    }

    private fun updateThread(threadTopic: String) {
        if (currentInvite != null) {
            val currentAccountId = currentInvite?.account?.value
            currentAccountId?.let { accountId ->
                if (currentAccountId != SELF_ACCOUNT) {
                    listOfThreads.add(ChatUI(R.drawable.ic_chat_icon_3, accountId, currentInvite!!.message, null))
                    _listOfThreadsStateFlow.value = listOfThreads.toList()
                    userNameToTopicMap.put(accountId, threadTopic)
                } else {
                    listOfThreads.add(ChatUI(R.drawable.ic_chat_icon_3, whoWasInvitedContact!!, currentInvite!!.message, null))
                    _listOfThreadsStateFlow.value = listOfThreads.toList()
                    userNameToTopicMap.put(whoWasInvitedContact!!, threadTopic)
                }
            }
        } else Log.e(tag(this), "Unable to find currentInvite or invited contact")
    }

    private fun updateInvites(id: Long?) {
        listOfInvites.removeIf { invite -> invite.id == id }
        _listOfInvitesStateFlow.value = listOfInvites.toList()
    }

    companion object {
        private const val KOTLIN_ACCOUNT_ID = "eip155:2:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"
        private const val SWIFT_ACCOUNT_ID = "eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"
        private const val JS_ACCOUNT_ID = "eip155:3:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"
        val SELF_ACCOUNT = KOTLIN_ACCOUNT_ID
    }
}