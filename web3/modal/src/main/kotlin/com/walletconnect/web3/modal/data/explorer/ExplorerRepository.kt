package com.walletconnect.web3.modal.data.explorer

import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.network.ExplorerService
import com.walletconnect.web3.modal.network.model.ExplorerResponse
internal class ExplorerRepository(
    private val explorerApi: ExplorerService,
) {
    private val projectId by lazy { wcKoinApp.koin.get<ProjectId>() }

    suspend fun getWalletsList(
        page: Int = 1,
        entries: Int,
        chains: List<String>
    ): ExplorerResponse? {
        return explorerApi.getWallets(
            projectId = projectId.value,
            page = page,
            entries = entries,
            chains = chains.joinToString(",")
        ).body()
    }
}
