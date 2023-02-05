@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.data.service


import com.walletconnect.chat.discovery.keyserver.model.KeyServerBody
import com.walletconnect.chat.discovery.keyserver.model.KeyServerHttpResponse
import retrofit2.Response
import retrofit2.http.*

internal interface KeyServerService {
    @Headers("Content-Type: application/json")
    @POST("invite")
    suspend fun registerInvite(@Body body: KeyServerBody.RegisterInvite)
    @GET("invite")
    suspend fun resolveInvite(@Query("account") account: String): Response<KeyServerHttpResponse.ResolveInvite>
    @GET("identity")
    suspend fun resolveIdentity(@Query("publicKey") publicKey: String): Response<KeyServerHttpResponse.ResolveIdentity>
    @Headers("Content-Type: application/json")
    @POST("identity")
    suspend fun registerIdentity(@Body body: KeyServerBody.RegisterIdentity)
}