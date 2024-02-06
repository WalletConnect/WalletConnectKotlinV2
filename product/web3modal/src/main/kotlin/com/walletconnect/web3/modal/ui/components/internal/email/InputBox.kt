package com.walletconnect.web3.modal.ui.components.internal.email

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.inputs.InputState
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun InputValidationBox(
    inputState: InputState,
    errorMessage: String,
    errorAlign: TextAlign = TextAlign.Left,
    content: @Composable () -> Unit,
) {
    val hasError by inputState.hasError.collectAsState()

    Column(modifier = Modifier.animateContentSize()) {
        content()
        if (hasError) {
            VerticalSpacer(height = 4.dp)
            Text(
                text = errorMessage,
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .fillMaxWidth(),
                style = Web3ModalTheme.typo.tiny400.copy(color = Web3ModalTheme.colors.error, textAlign = errorAlign)
            )
        }
    }
}

