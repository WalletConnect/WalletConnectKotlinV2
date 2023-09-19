package com.walletconnect.android.internal.common.explorer

import com.walletconnect.android.internal.common.explorer.data.model.Project

interface ExplorerInterface {
    suspend fun getProjects(page: Int, entries: Int, isVerified: Boolean): Result<List<Project>>
}