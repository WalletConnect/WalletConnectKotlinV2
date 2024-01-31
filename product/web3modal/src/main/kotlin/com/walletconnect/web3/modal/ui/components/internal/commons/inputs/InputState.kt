package com.walletconnect.web3.modal.ui.components.internal.commons.inputs

import com.walletconnect.util.Empty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal abstract class InputState(
    private val coroutineScope: CoroutineScope
) {

    protected val mutableState: MutableStateFlow<InputData> = MutableStateFlow(InputData())

    val state: StateFlow<InputData>
        get() = mutableState.asStateFlow()

    val hasError: StateFlow<Boolean>
        get() = state.map { it.hasError }.stateIn(coroutineScope, SharingStarted.Lazily, false)


    fun onTextChange(value: String) {
        mutableState.update { it.copy(text = value) }
    }

    fun onFocusChange(isFocused: Boolean) {
        mutableState.update { it.copy(isFocused = isFocused) }
    }

    abstract fun submit()
}

internal data class InputData(
    val text: String = String.Empty,
    val isFocused: Boolean = false,
    val hasError: Boolean = false
)
