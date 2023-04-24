package com.walletconnect.sample.dapp.web3modal.data.explorer

import com.walletconnect.sample.dapp.BuildConfig
import com.walletconnect.sample.dapp.web3modal.network.ExplorerService
import com.walletconnect.sample.dapp.web3modal.network.model.ExplorerResponse

class ExplorerRepository(
    private val explorerApi: ExplorerService
) {

    private val projectId = BuildConfig.PROJECT_ID

    suspend fun getWalletsList(
        page: Int = 1,
        entries: Int,
        chains: List<String>
    ): ExplorerResponse? {
        return explorerApi.getWallets(
            projectId = projectId,
            page = page,
            entries = entries,
            chains = chains.joinToString(",")
        ).body()
    }

}