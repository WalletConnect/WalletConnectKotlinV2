package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.web3.modal.domain.SessionRepository

internal class SaveChainSelectionUseCase(
    private val repository: SessionRepository
) {
    operator fun invoke(chain: String) {
        repository.saveChainSelection(chain)
    }
}