@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.data.service


import com.walletconnect.chat.discovery.keyserver.model.KeyServerDTO
import retrofit2.http.*

internal interface KeyServerService {

    @Headers("Content-Type: application/json")
    @POST("invite")
    suspend fun registerInvite(@Body body: KeyServerDTO.RegisterInviteBody)


    @GET("invite")
    suspend fun resolveInvite(@Query("account") account: String): KeyServerDTO.ResolveInviteResponse

    @GET("identity")
    suspend fun resolveIdentity(@Query("publicKey") publicKey: String): KeyServerDTO.ResolveIdentityResponse

    @Headers("Content-Type: application/json")
    @POST("identity")
    suspend fun registerIdentity(@Body body: KeyServerDTO.RegisterIdentityBody)
}