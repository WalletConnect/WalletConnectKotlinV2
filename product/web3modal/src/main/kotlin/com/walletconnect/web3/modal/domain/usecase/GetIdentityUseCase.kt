package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.web3.modal.data.BlockchainRepository
internal class GetIdentityUseCase(
    private val blockchainRepository: BlockchainRepository
) {
    suspend operator fun invoke(address: String, chainId: String) = blockchainRepository.getIdentity(address = address, chainId = chainId)
}
