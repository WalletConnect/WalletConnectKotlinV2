package com.walletconnect.web3.modal.data

import com.walletconnect.android.BuildConfig
import com.walletconnect.web3.modal.data.model.IdentityDTO
import com.walletconnect.web3.modal.data.network.BlockchainService
import com.walletconnect.web3.modal.domain.model.Identity

internal class BlockchainRepository(
    private val blockchainService: BlockchainService
) {

    suspend fun getIdentity(address: String, chainId: String) = with(
        blockchainService.getIdentity(address = address, chainId = chainId, projectId = BuildConfig.PROJECT_ID)
    ) {
        if (isSuccessful && body() != null) {
            body()!!.toIdentity()
        } else {
            throw Throwable(errorBody()?.string())
        }
    }
}

private fun IdentityDTO.toIdentity() = Identity(name, avatar)
