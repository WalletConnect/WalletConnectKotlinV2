package com.walletconnect.android.internal.common.explorer.domain.usecase

import com.walletconnect.android.internal.common.explorer.ExplorerRepository
import com.walletconnect.android.internal.common.explorer.data.model.Project

class GetProjectsWithPaginationUseCase(
    private val explorerRepository: ExplorerRepository,
) {
    suspend operator fun invoke(page: Int, entries: Int, isVerified: Boolean): Result<List<Project>> =
        runCatching { explorerRepository.getProjects(page, entries, isVerified).projects }
}
