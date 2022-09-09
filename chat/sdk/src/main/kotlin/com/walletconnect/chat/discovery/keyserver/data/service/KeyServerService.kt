@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.data.service


import com.walletconnect.chat.discovery.keyserver.model.KeyServerDTO
import retrofit2.http.*

internal interface KeyServerService {

    @Headers("Content-Type: application/json")
    @POST("register")
    suspend fun register(@Body account: KeyServerDTO.Account)

    @GET("resolve")
    suspend fun resolve(@Query("account") account: String): KeyServerDTO.Account
}