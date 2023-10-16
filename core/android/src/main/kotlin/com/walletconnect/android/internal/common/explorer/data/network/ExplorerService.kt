@file:JvmSynthetic

package com.walletconnect.android.internal.common.explorer.data.network

import com.walletconnect.android.internal.common.explorer.data.network.model.DappListingsDTO
import com.walletconnect.android.internal.common.explorer.data.network.model.NotifyConfigDTO
import com.walletconnect.android.internal.common.explorer.data.network.model.ProjectListingDTO
import com.walletconnect.android.internal.common.explorer.data.network.model.WalletListingDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ExplorerService {

    @GET("v3/dapps")
    suspend fun getAllDapps(@Query("projectId") projectId: String): Response<DappListingsDTO>

    @GET("w3i/v1/projects")
    suspend fun getProjects(
        @Query("projectId") projectId: String,
        @Query("entries") entries: Int,
        @Query("page") page: Int,
        @Query("is_verified") isVerified: Boolean,
    ): Response<ProjectListingDTO>

    @GET("w3i/v1/notify-config")
    suspend fun getNotifyConfig(
        @Query("projectId") projectId: String,
        @Query("appDomain") appDomain: String,
    ): Response<NotifyConfigDTO>

    @GET("w3m/v1/getAndroidListings")
    suspend fun getAndroidWallets(
        @Query("projectId") projectId: String,
        @Query("chains") chains: String?,
        @Query("sdkType") sdkType: String,
        @Query("sdkVersion") sdkVersion: String,
        @Query("excludedIds") excludedIds: String?,
        @Query("recommendedIds") recommendedIds: String?,
    ): Response<WalletListingDTO>
}