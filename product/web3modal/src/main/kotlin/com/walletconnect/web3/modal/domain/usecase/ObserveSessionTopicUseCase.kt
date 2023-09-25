package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.web3.modal.domain.SessionRepository

internal class ObserveSessionTopicUseCase(
    private val repository: SessionRepository
) {
    operator fun invoke() = repository.sessionTopic
}