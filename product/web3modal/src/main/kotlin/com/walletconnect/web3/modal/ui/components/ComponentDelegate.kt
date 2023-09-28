package com.walletconnect.web3.modal.ui.components

import com.walletconnect.web3.modal.client.Web3Modal
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onEach

internal sealed class ComponentEvent(val isOpen: Boolean) {
    object ModalHiddenEvent: ComponentEvent(false)
    object ModalExpandedEvent: ComponentEvent(true)
}

internal object ComponentDelegate {

    val modalComponentEvent: MutableSharedFlow<ComponentEvent> = MutableSharedFlow()

    var isModalOpen: Boolean = false

    fun setDelegate(delegate: Web3Modal.ComponentDelegate) {
        modalComponentEvent.onEach { event ->
            when(event) {
                ComponentEvent.ModalHiddenEvent -> delegate.onModalHidden()
                ComponentEvent.ModalExpandedEvent -> delegate.onModalExpanded()
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


