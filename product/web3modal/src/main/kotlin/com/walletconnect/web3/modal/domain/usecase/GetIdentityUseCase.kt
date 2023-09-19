package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.web3.modal.data.BlockchainRepository
import com.walletconnect.web3.modal.domain.model.Identity

internal class GetIdentityUseCase(
    private val blockchainRepository: BlockchainRepository
) {
    suspend operator fun invoke(address: String, chainId: String) = try {
        blockchainRepository.getIdentity(address = address, chainId = chainId)
    } catch (e: Throwable) {
        Identity()
    }
}
