package com.walletconnect.web3.modal.ui.components.internal.email.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.walletconnect.web3.modal.ui.components.internal.commons.inputs.InputState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update

@Composable
internal fun rememberEmailInputState(
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): EmailInputState {
    return remember(coroutineScope) {
        EmailInputState(coroutineScope)
    }
}

internal class EmailInputState(coroutineScope: CoroutineScope): InputState(coroutineScope) {

    fun validateEmail() {
        mutableState.update { it }
    }

    override fun submit() {

    }

}
