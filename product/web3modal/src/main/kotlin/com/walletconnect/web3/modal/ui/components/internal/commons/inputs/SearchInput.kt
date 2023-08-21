package com.walletconnect.web3.modal.ui.components.internal.commons.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.walletconnect.modal.ui.components.common.HorizontalSpacer
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.ContentDescription
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun SearchInput(
    searchValue: String,
    onSearchValueChange: (String) -> Unit,
    onClearClick: () -> Unit,
    isEnabled: Boolean = true
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    val borderColor: Color
    val backgroundColor: Color

    when {
        isFocused -> {
            borderColor = Web3ModalTheme.colors.main100
            backgroundColor = Web3ModalTheme.colors.overlay05
        }
        isEnabled -> {
            borderColor = Web3ModalTheme.colors.overlay05
            backgroundColor = Web3ModalTheme.colors.overlay05
        }
        else -> {
            borderColor = Web3ModalTheme.colors.overlay10
            backgroundColor = Web3ModalTheme.colors.overlay15
        }
    }

    BasicTextField(
        value = searchValue,
        onValueChange = onSearchValueChange,
        textStyle = TextStyle(color = Web3ModalTheme.colors.foreground.color100),
        cursorBrush = SolidColor(Web3ModalTheme.colors.main100),
        singleLine = true,
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus(true) }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .padding(2.dp)
            .onFocusChanged { isFocused = it.hasFocus }
            .focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalSpacer(width = 12.dp)
                Icon(
                    tint = Web3ModalTheme.colors.foreground.color275,
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_search),
                    contentDescription = ContentDescription.SEARCH.description,
                )
                HorizontalSpacer(width = 8.dp)
                Box(modifier = Modifier.weight(1f)) {
                    if (searchValue.isBlank()) {
                        Text(text = "Search wallets", style = TextStyle(color = Web3ModalTheme.colors.foreground.color275))
                    }
                    innerTextField()
                    if (searchValue.isNotBlank()) {
                        InputCancel(modifier = Modifier.align(Alignment.CenterEnd)) {
                            onSearchValueChange(String.Empty)
                            onClearClick.invoke()
                        }
                    }
                }
                HorizontalSpacer(width = 12.dp)
            }
        },
    )
//    }
}

@Composable
@UiModePreview
private fun PreviewSearchInput() {
    MultipleComponentsPreview(
        { SearchInput(searchValue = "", onSearchValueChange = {}, onClearClick = {}) },
        { SearchInput(searchValue = "Ledger", onSearchValueChange = {}, onClearClick = {}) },
    )
}
