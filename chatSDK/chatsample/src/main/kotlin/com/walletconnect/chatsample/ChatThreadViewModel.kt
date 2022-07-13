package com.walletconnect.chatsample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.chatsample.ui.ThreadUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatThreadViewModel: ViewModel() {
    private val _listOfMessages = MutableStateFlow(listOf<ThreadUI>())
    val listOfMessages: StateFlow<List<ThreadUI>> = _listOfMessages.asStateFlow()

    fun addSelfMessage(message: String) {
        _listOfMessages.update {
            it.plus(ThreadUI.Self(message))
        }

        viewModelScope.launch(Dispatchers.IO) {
            delay(1500)

            _listOfMessages.update {
                it.plus(ThreadUI.Peer(R.drawable.ic_chat_1_mini, " Why, List, why, WHY?!"))
            }
        }
    }
}