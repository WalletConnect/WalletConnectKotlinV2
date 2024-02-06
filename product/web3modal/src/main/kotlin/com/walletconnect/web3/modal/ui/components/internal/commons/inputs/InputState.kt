package com.walletconnect.web3.modal.ui.components.internal.commons.inputs

import androidx.compose.ui.focus.FocusManager
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
    protected val coroutineScope: CoroutineScope,
    protected val focusManager: FocusManager
) {

    protected val mutableState: MutableStateFlow<InputData> = MutableStateFlow(InputData())

    val state: StateFlow<InputData>
        get() = mutableState.asStateFlow()

    val hasError: StateFlow<Boolean>
        get() = state.map { it.hasError }.stateIn(coroutineScope, SharingStarted.Lazily, false)


    open fun onTextChange(value: String) {
        mutableState.update { it.copy(text = value, hasError = false) }
    }

    open fun onFocusChange(isFocused: Boolean) {
        mutableState.update { it.copy(isFocused = isFocused) }
    }

    fun clearFocus() {
        mutableState.update { it.copy(isFocused = false) }
        focusManager.clearFocus(true)
    }

    abstract fun submit(text: String)
}

internal data class InputData(
    val text: String = String.Empty,
    val isFocused: Boolean = false,
    val hasError: Boolean = false
)
