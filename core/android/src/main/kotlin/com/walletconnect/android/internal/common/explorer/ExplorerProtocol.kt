package com.walletconnect.android.internal.common.explorer

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.explorer.data.model.Project
import com.walletconnect.android.internal.common.explorer.domain.usecase.GetProjectsWithPaginationUseCase
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named


//discuss: Opening more endpoints to SDK consumers
class ExplorerProtocol(
    private val koinApp: KoinApplication = wcKoinApp,
) : ExplorerInterface {
    private val getProjectsWithPaginationUseCase: GetProjectsWithPaginationUseCase by lazy { koinApp.koin.get() }
    private val logger: Logger by lazy { koinApp.koin.get(named(AndroidCommonDITags.LOGGER)) }

    override suspend fun getProjects(page: Int, entries: Int, isVerified: Boolean): Result<List<Project>> = getProjectsWithPaginationUseCase(page, entries, isVerified)
}


