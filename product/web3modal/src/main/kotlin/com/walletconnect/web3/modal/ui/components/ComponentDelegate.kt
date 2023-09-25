package com.walletconnect.web3.modal.ui.components

import com.walletconnect.web3.modal.client.Web3Modal
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onEach

internal sealed class ComponentEvent {
    object ModalHiddenEvent: ComponentEvent()
    object ModalExpandedEvent: ComponentEvent()
}

internal object ComponentDelegate {

    val modalComponentEvent: MutableSharedFlow<ComponentEvent> = MutableSharedFlow()

    var isModalOpen: Boolean = false

    fun setDelegate(delegate: Web3Modal.ComponentDelegate) {
        modalComponentEvent.onEach {
            when(it) {
                ComponentEvent.ModalHiddenEvent -> delegate.onModalHidden().also { isModalOpen = false }
                ComponentEvent.ModalExpandedEvent -> delegate.onModalExpanded().also { isModalOpen = true }
            }
        }
    }

    suspend fun openModalEvent() {
        modalComponentEvent.emit(ComponentEvent.ModalExpandedEvent)
        isModalOpen = true
    }

    suspend fun closeModalEvent() {
        modalComponentEvent.emit(ComponentEvent.ModalHiddenEvent)
        isModalOpen = false
    }
}


