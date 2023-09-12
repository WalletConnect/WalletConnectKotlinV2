package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.web3.modal.domain.SessionRepository

internal class SaveSessionTopicUseCase(
    private val repository: SessionRepository
) {
    operator fun invoke(topic: String) {
        repository.saveSessionTopic(topic)
    }
}