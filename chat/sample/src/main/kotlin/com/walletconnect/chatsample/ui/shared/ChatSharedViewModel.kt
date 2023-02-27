package com.walletconnect.chatsample.ui.shared

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.cacao.sign
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.chat.cacao.CacaoSigner
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.domain.ChatDelegate
import com.walletconnect.chatsample.utils.tag
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.Keys
import java.security.Security

class ChatSharedViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("Chat_Shared_Prefs", Context.MODE_PRIVATE)
    var sentInvite: Chat.Params.Invite? = null
    var receivedInvite: Chat.Model.Invite.Received? = null
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

    private lateinit var _account: String
    private lateinit var _publicKey: String
    private lateinit var _privateKey: String

    fun getLastMessage(peerName: String) = listOfMessages.last { it.peerName == peerName }

    val emittedEvents: Flow<ChatSharedEvents> = ChatDelegate.wcEventModels.map { walletEvent: Chat.Model.Events ->
        Log.d(TAG, walletEvent.toString())
        when (walletEvent) {
            is Chat.Model.Events.OnInvite -> walletEvent.toChatSharedEvents().also { onInvite(it) }
            is Chat.Model.Events.OnInviteAccepted -> walletEvent.toChatSharedEvents().also { onInviteAccepted(it) }
            is Chat.Model.Events.OnMessage -> walletEvent.toChatSharedEvents().also { onMessage(it) }
            is Chat.Model.Events.OnInviteRejected -> walletEvent.toChatSharedEvents()
            else -> ChatSharedEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    fun register() {
        val account = sharedPreferences.getString(ACCOUNT_TAG, null)
        val publicKey = sharedPreferences.getString(PUBLIC_KEY_TAG, null)
        val privateKey = sharedPreferences.getString(PRIVATE_KEY_TAG, null)

        if (account != null && publicKey != null && privateKey != null) {
            _account = account
            _publicKey = publicKey
            _privateKey = privateKey
            // Note: This is only demo. Normally you want more security with private key
        }
        registerIdentity()
    }

    private fun goPublic() {
        val goPublic = Chat.Params.GoPublic(Chat.Type.AccountId(_account))
        ChatClient.goPublic(
            goPublic,
            { publicKey -> Log.d(TAG, "Registered invite successfully, account: $_account, inviteKey: $publicKey") },
            { error -> Log.e(TAG, "Register error: ${error.throwable.stackTraceToString()}") }
        )
    }

    private fun registerIdentity() {
        val keypair = generateKeys()
        _publicKey = keypair.first
        _privateKey = keypair.second
        _account = generateEthereumAccount(keypair.third)

        sharedPreferences.edit().putString(ACCOUNT_TAG, _account).apply()
        sharedPreferences.edit().putString(PUBLIC_KEY_TAG, _publicKey).apply()
        sharedPreferences.edit().putString(PRIVATE_KEY_TAG, _privateKey).apply()
        // Note: This is only demo. Normally you want more security with private key.

        val register = Chat.Params.Register(Chat.Type.AccountId(_account))
        ChatClient.register(register, object : Chat.Listeners.Register {
            override fun onSign(message: String): Chat.Model.Cacao.Signature? {
                return CacaoSigner.sign(message, _privateKey.hexToBytes(), SignatureType.EIP191)
            }

            override fun onError(error: Chat.Model.Error) {
                Log.e(TAG, "Register error: ${error.throwable.stackTraceToString()}")
            }

            override fun onSuccess(publicKey: String) {
                Log.d(TAG, "Registered identity successfully, account: $_account, identityKey: $publicKey")
            }
        })
    }

    fun invite(contact: String, openingMessage: String, afterInviteSent: () -> Unit) {
        ChatClient.resolve(Chat.Params.Resolve(Chat.Type.AccountId(contact)), object : Chat.Listeners.Resolve {
            override fun onError(error: Chat.Model.Error) {
                Log.e(TAG, error.throwable.stackTraceToString())
            }

            override fun onSuccess(publicKey: String) {
                val invite = Chat.Params.Invite(inviterAccount = Chat.Type.AccountId(_account), inviteeAccount = Chat.Type.AccountId(contact), Chat.Type.InviteMessage(openingMessage), publicKey)
                ChatClient.invite(
                    invite,
                    { Log.d(TAG, "Invited, inviter: $_account, invitee: $contact") },
                    { error -> Log.e(tag(this@ChatSharedViewModel), error.throwable.stackTraceToString()) }
                )

                runBlocking(Dispatchers.Main) {
                    sentInvite = invite
                    whoWasInvitedContact = contact
                    Log.e(TAG, "invite: $sentInvite")
                    listOfMessages.add(MessageUI(contact, invite.message.value, System.currentTimeMillis(), contact))
                    _listOfMessagesStateFlow.value = listOfMessages.toList()
                    afterInviteSent()
                }
            }
        })
    }

    fun acceptInvitation(chatUI: ChatUI) {
        chatUI.id?.let { id ->
            ChatClient.accept(Chat.Params.Accept(id), onSuccess = { threadTopic -> updateThread(threadTopic) }, { error ->
                Log.e(TAG, error.throwable.stackTraceToString())
            })
            updateInvites(chatUI.id)
        } ?: Log.e(TAG, "Unable to find id on accpet invitation: $chatUI")
    }

    fun rejectInvitation(id: Long?) {
        id?.let {
            val reject = Chat.Params.Reject(it)
            ChatClient.reject(reject) { error -> Log.e(TAG, "Unable to find reject invitation: $error") }
            updateInvites(id)
        } ?: Log.e(TAG, "Unable to find id on reject invitation: $id")
    }

    fun sendMessage(message: String, peerName: String) {
        Log.e(TAG, "sendMessage: $peerName")

        val (userName, topic) = userNameToTopicMap.entries.single { entry -> entry.key == peerName }
        listOfMessages.add(MessageUI(userName, message, System.currentTimeMillis(), _account))
        _listOfMessagesStateFlow.value = listOfMessages.toList()

        ChatClient.message(Chat.Params.Message(topic, Chat.Type.ChatMessage(message))) { error ->
            Log.e(TAG, error.throwable.stackTraceToString())
        }
    }

    private fun onInvite(event: ChatSharedEvents.OnInvite) {
        Log.d(TAG, "Invited: ${event.invite.message}")
        val contact = event.invite.inviterAccount.value
        receivedInvite = event.invite

        listOfInvites.add(ChatUI(R.drawable.ic_chat_icon_3, contact, event.invite.message.value, event.invite.id))
        _listOfInvitesStateFlow.update { listOfInvites.toList() }

        listOfMessages.add(MessageUI(contact, event.invite.message.value, System.currentTimeMillis(), contact))
        _listOfMessagesStateFlow.value = listOfMessages.toList()
    }

    private fun onInviteAccepted(event: ChatSharedEvents.OnInviteAccepted) {
        Log.d(TAG, "Joined: ${event.topic}")
        updateThread(event.topic)
    }

    private fun onMessage(event: ChatSharedEvents.OnMessage) {
        Log.d(TAG, "Message: ${event.message.message}")

        userNameToTopicMap.entries.find { it.value == event.message.topic }?.let { (_, topic) ->
            listOfThreads.find { topic == event.message.topic }?.let {
                val author = event.message.authorAccount.value
                listOfMessages.add(MessageUI(author, event.message.message.value, event.message.timestamp, author))
                _listOfMessagesStateFlow.value = listOfMessages.toList()
            } ?: Log.e(TAG, "Unable to find topic: ${event.message.topic}")
        }
    }

    private fun updateThread(threadTopic: String) { //todo: simplify
        if (sentInvite != null) {
            val currentAccountId = sentInvite?.inviterAccount?.value
            currentAccountId?.let { accountId ->
                if (currentAccountId != _account) {
                    listOfThreads.add(ChatUI(R.drawable.ic_chat_icon_3, accountId, sentInvite!!.message.value, null))
                    _listOfThreadsStateFlow.value = listOfThreads.toList()
                    userNameToTopicMap.put(accountId, threadTopic)
                } else {
                    listOfThreads.add(ChatUI(R.drawable.ic_chat_icon_3, whoWasInvitedContact!!, sentInvite!!.message.value, null))
                    _listOfThreadsStateFlow.value = listOfThreads.toList()
                    userNameToTopicMap.put(whoWasInvitedContact!!, threadTopic)
                }
            }
        } else if (receivedInvite != null) {
            val currentAccountId = receivedInvite?.inviterAccount?.value
            currentAccountId?.let { accountId ->
                if (currentAccountId != _account) {
                    listOfThreads.add(ChatUI(R.drawable.ic_chat_icon_3, accountId, receivedInvite!!.message.value, null))
                    _listOfThreadsStateFlow.value = listOfThreads.toList()
                    userNameToTopicMap.put(accountId, threadTopic)
                } else {
                    listOfThreads.add(ChatUI(R.drawable.ic_chat_icon_3, whoWasInvitedContact!!, receivedInvite!!.message.value, null))
                    _listOfThreadsStateFlow.value = listOfThreads.toList()
                    userNameToTopicMap.put(whoWasInvitedContact!!, threadTopic)
                }
            }
        } else Log.e(TAG, "Unable to find currentInvite or invited contact")
    }

    private fun updateInvites(id: Long?) {
        listOfInvites.filter { invite -> invite.id == id }.forEach { listOfInvites.remove(it) }
        _listOfInvitesStateFlow.value = listOfInvites.toList()
    }


    private fun ByteArray.bytesToHex(): String {
        val hexString = StringBuilder(2 * this.size)

        this.indices.forEach { i ->
            val hex = Integer.toHexString(0xff and this[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }

    private fun String.hexToBytes(): ByteArray {
        val len = this.length
        val data = ByteArray(len / 2)
        var i = 0

        while (i < len) {
            data[i / 2] = ((Character.digit(this[i], 16) shl 4)
                    + Character.digit(this[i + 1], 16)).toByte()
            i += 2
        }

        return data
    }

    private fun generateEthereumAccount(address: String) = "eip155:1:0x$address"

    private fun generateKeys(): Triple<String, String, String> {
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider())
        val keypair = Keys.createEcKeyPair()
        val publicKey = PublicKey(keypair.publicKey.toByteArray().bytesToHex())
        val privateKey = PrivateKey(keypair.privateKey.toByteArray().bytesToHex())
        return Triple(publicKey.keyAsHex, privateKey.keyAsHex, Keys.getAddress(keypair))
    }

    companion object {
        private const val TAG = "ChatSharedViewModel"
        const val ACCOUNT_TAG = "self_account_tag"
        const val PRIVATE_KEY_TAG = "self_private_key"
        const val PUBLIC_KEY_TAG = "self_public_key"
    }
}