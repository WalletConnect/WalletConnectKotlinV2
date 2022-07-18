package com.walletconnect.chatsample.ui

import androidx.lifecycle.ViewModel
import com.walletconnect.chatsample.ChatUI
import com.walletconnect.chatsample.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChatSharedViewModel : ViewModel() {
    private val _listOfRequests = MutableStateFlow(listOf(
        ChatUI(R.drawable.ic_chat_icon_1, "JS.eth", "gm, my man!")
    ))
    val listOfRequests = _listOfRequests//.asStateFlow()

    private val _listOfChats: MutableStateFlow<List<ChatUI>> = MutableStateFlow(listOf(
        ChatUI(R.drawable.ic_chat_icon_2, "zeth.eth", "Chicken, Peter, you’re just a little chicken. Cheep, cheep, cheep, cheep")
    ))
    val listOfChats: MutableStateFlow<List<ChatUI>> = _listOfChats//.asStateFlow()

    fun acceptRequest(username: String) {
        var request: ChatUI? = null

        _listOfRequests.update { listOfRequests ->
            request = listOfRequests.first { it.username == username }

            if (request != null) {
                listOfRequests.minus(request!!)
            } else {
                listOfRequests
            }
        }

        _listOfChats.update { setOfChats ->
            if (request != null) {
                setOfChats.plus(request!!)
            } else {
                setOfChats
            }
        }
    }
}