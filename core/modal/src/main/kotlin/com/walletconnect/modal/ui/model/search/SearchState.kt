package com.walletconnect.modal.ui.model.search

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchState(
    searchPhrase: String = "",
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
        onSearchValueChange("")
    }
}

data class SearchData(
    val searchValue: String,
    val isFocused: Boolean,
)
