package com.walletconnect.web3.modal.ui.components.internal.commons.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.walletconnect.modal.ui.components.common.HorizontalSpacer
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.ContentDescription
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun BaseTextInput(
    inputState: InputState,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isEnabled: Boolean = true,
    content: @Composable (innerTextField: @Composable () -> Unit, inputData: InputData) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val borderColor: Color
    val backgroundColor: Color
    val state by inputState.state.collectAsState()

    when {
        state.isFocused -> {
            borderColor = Web3ModalTheme.colors.accent100
            backgroundColor = Web3ModalTheme.colors.grayGlass10
        }

        isEnabled -> {
            borderColor = Web3ModalTheme.colors.grayGlass05
            backgroundColor = Web3ModalTheme.colors.grayGlass05
        }

        else -> {
            borderColor = Web3ModalTheme.colors.grayGlass10
            backgroundColor = Web3ModalTheme.colors.grayGlass15
        }
    }

    BasicTextField(value = state.text,
        onValueChange = inputState::onTextChange,
        textStyle = Web3ModalTheme.typo.paragraph400.copy(color = Web3ModalTheme.colors.foreground.color100),
        cursorBrush = SolidColor(Web3ModalTheme.colors.accent100),
        singleLine = true,
        keyboardActions = KeyboardActions {
            inputState.submit()
            focusManager.clearFocus(true)
        },
        keyboardOptions = keyboardOptions,
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .onFocusChanged { inputState.onFocusChange(it.hasFocus) }
            .focusRequester(focusRequester),
        decorationBox = {
            content(it, state)
        })
}
