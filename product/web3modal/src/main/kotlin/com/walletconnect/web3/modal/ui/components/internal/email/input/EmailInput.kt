package com.walletconnect.web3.modal.ui.components.internal.email.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.walletconnect.modal.ui.components.common.HorizontalSpacer
import com.walletconnect.modal.ui.components.common.roundedClickable
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.ContentDescription
import com.walletconnect.web3.modal.ui.components.internal.commons.LoadingSpinner
import com.walletconnect.web3.modal.ui.components.internal.commons.inputs.BaseTextInput
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun EmailInput(
    emailInputState: EmailInputState,
    isLoading: Boolean = false,
    isEnabled: Boolean = true
) {
    BaseTextInput(
        inputState = emailInputState,
        modifier = Modifier.height(50.dp),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Email
        ),
        isEnabled = isEnabled,
    ) { innerTextField, inputData ->
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalSpacer(width = 10.dp)
            Icon(
                tint = Web3ModalTheme.colors.foreground.color275,
                modifier = Modifier.size(14.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_email),
                contentDescription = ContentDescription.EMAIL.description,
            )
            HorizontalSpacer(width = 6.dp)
            Box(modifier = Modifier.weight(1f)) {
                if (inputData.text.isBlank()) {
                    Text(text = "Email", style = Web3ModalTheme.typo.paragraph400.copy(color = Web3ModalTheme.colors.foreground.color275))
                }
                innerTextField()
            }
            when {
                isLoading -> LoadingIcon()
                inputData.text.isNotBlank() && !inputData.hasError -> ForwardIcon { emailInputState.submit(inputData.text) }
            }
            HorizontalSpacer(width = 10.dp)
        }

    }
}

@Composable
private fun ForwardIcon(onClick: () -> Unit) {
    Icon(
        tint = Web3ModalTheme.colors.accent100,
        modifier = Modifier
            .size(14.dp)
            .roundedClickable { onClick() },
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_chevron_right),
        contentDescription = ContentDescription.CHEVRON_RIGHT.description,
    )
}

@Composable
private fun LoadingIcon() {
    LoadingSpinner(size = 14.dp)
}
