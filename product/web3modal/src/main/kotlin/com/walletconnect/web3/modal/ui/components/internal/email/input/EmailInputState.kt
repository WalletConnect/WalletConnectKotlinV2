package com.walletconnect.web3.modal.ui.components.internal.email.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import com.walletconnect.web3.modal.ui.components.internal.commons.inputs.InputState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update

@Composable
internal fun rememberEmailInputState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    focusManager: FocusManager = LocalFocusManager.current,
    onSubmit: (String) -> Unit
): EmailInputState {
    return remember(coroutineScope, focusManager) {
        EmailInputState(coroutineScope, focusManager, onSubmit)
    }
}

internal class EmailInputState(
    coroutineScope: CoroutineScope,
    focusManager: FocusManager,
    private val onSubmit: (String) -> Unit
): InputState(coroutineScope, focusManager) {

    fun validateEmail(text: String): Boolean {
        val isValid = text.isNotEmpty()
        mutableState.update { it.copy(hasError = isValid) }
        return isValid
    }

    override fun submit(text: String) {
        clearFocus()
        if (validateEmail(text)) {
            onSubmit(text)
        }
    }

}
