package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.web3.modal.domain.SessionRepository
import com.walletconnect.web3.modal.domain.model.Session

internal class SaveSessionUseCase(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(session: Session) = repository.saveSession(session)
}