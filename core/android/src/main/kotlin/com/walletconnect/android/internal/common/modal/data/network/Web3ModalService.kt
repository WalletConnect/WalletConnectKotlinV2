package com.walletconnect.android.internal.common.modal.data.network

import com.walletconnect.android.internal.common.modal.data.network.model.GetAndroidDataDTO
import com.walletconnect.android.internal.common.modal.data.network.model.GetWalletsDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

internal interface Web3ModalService {
    @GET("getWallets")
    suspend fun getWallets(
        @Header("x-sdk-type") sdkType: String,
        @Query("page") page: Int,
        @Query("search") search: String? = null,
        @Query("exclude") exclude: String? = null,
        @Query("include") include: String? = null,
        @Query("entries") entries: Int = 100,
        @Query("platform") platform: String = "android"
    ): Response<GetWalletsDTO>

    @GET("getAndroidData")
    suspend fun getAndroidData(
        @Header("x-sdk-type") sdkType: String,
    ): Response<GetAndroidDataDTO>
}
