package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.web3.modal.domain.SessionRepository
import kotlinx.coroutines.flow.map

internal class ObserveSelectedChainUseCase(
    private val repository: SessionRepository
) {
    operator fun invoke() = repository.session.map { it?.chain }
}