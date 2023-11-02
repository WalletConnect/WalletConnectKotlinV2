package com.walletconnect.web3.modal.ui.components.internal.commons.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.walletconnect.modal.ui.components.common.HorizontalSpacer
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.ContentDescription
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class SearchState(
    searchPhrase: String = String.Empty,
    private val onSearchSubmit: (String) -> Unit,
    private val onClearInput: () -> Unit
) {
    private val _state: MutableStateFlow<SearchData> = MutableStateFlow(SearchData(searchPhrase, false))

    val state: StateFlow<SearchData>
        get() = _state.asStateFlow()

    val isFocused: Boolean
        get() = _state.value.isFocused

    val searchValue: String
        get() = _state.value.searchValue

    fun onSearchValueChange(value: String) {
        _state.update { it.copy(searchValue = value) }
    }

    fun onSearchSubmit() {
        onSearchSubmit(searchValue)
    }

    fun onFocusChange(isFocused: Boolean) {
        _state.update { it.copy(isFocused = isFocused) }
    }

    fun onSearchClearInput() {
        onClearInput()
        onSearchValueChange(String.Empty)
    }
}

internal data class SearchData(
    val searchValue: String,
    val isFocused: Boolean,
)

@Composable
internal fun SearchInput(
    searchState: SearchState,
    isEnabled: Boolean = true
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val borderColor: Color
    val backgroundColor: Color
    val state by searchState.state.collectAsState()

    when {
        searchState.isFocused -> {
            borderColor = Web3ModalTheme.colors.accent100
            backgroundColor = Web3ModalTheme.colors.grayGlass05
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

    BasicTextField(
        value = state.searchValue,
        onValueChange = searchState::onSearchValueChange,
        textStyle = Web3ModalTheme.typo.paragraph400.copy(color = Web3ModalTheme.colors.foreground.color100),
        cursorBrush = SolidColor(Web3ModalTheme.colors.accent100),
        singleLine = true,
        keyboardActions = KeyboardActions(onSearch = {
            searchState.onSearchSubmit()
            focusManager.clearFocus(true) }
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier
            .height(40.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .onFocusChanged { searchState.onFocusChange(it.hasFocus) }
            .focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalSpacer(width = 6.dp)
                Icon(
                    tint = Web3ModalTheme.colors.foreground.color275,
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_search),
                    contentDescription = ContentDescription.SEARCH.description,
                )
                HorizontalSpacer(width = 8.dp)
                Box(modifier = Modifier.weight(1f)) {
                    if (state.searchValue.isBlank()) {
                        Text(text = "Search wallets", style = Web3ModalTheme.typo.paragraph400.copy(color = Web3ModalTheme.colors.foreground.color275))
                    }
                    innerTextField()
                    if (state.searchValue.isNotBlank()) {
                        InputCancel(modifier = Modifier.align(Alignment.CenterEnd)) {
                            searchState.onSearchClearInput()
                        }
                    }
                }
                HorizontalSpacer(width = 6.dp)
            }
        },
    )
}

@Composable
@UiModePreview
private fun PreviewSearchInput(
    @PreviewParameter(SearchStatePreviewProvider::class) state: SearchState
) {
    ComponentPreview { SearchInput(searchState = state) }
}

internal class SearchStatePreviewProvider : PreviewParameterProvider<SearchState> {
    override val values: Sequence<SearchState> = sequenceOf(
        SearchState("", {}, {}),
        SearchState("", {}, {}).apply { onFocusChange(true) },
        SearchState("Search text", {}, {}).apply { onFocusChange(true) },
        SearchState("Search text", {}, {})
    )
}
