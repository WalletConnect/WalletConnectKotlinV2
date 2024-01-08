package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.web3.modal.domain.SessionRepository

internal class GetSessionUseCase(
    private val repository: SessionRepository
) {
    operator fun invoke() = repository.getSession()
}
