@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.data.service


import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.cacao.Cacao
import retrofit2.http.*

internal interface KeyServerService {

    @Headers("Content-Type: application/json")
    @POST("invite")
    suspend fun registerInvite(@Body body: RegisterInviteBody)
    @JsonClass(generateAdapter = true)
    data class RegisterInviteBody(val idAuth: String)

    @GET("invite")
    suspend fun resolveInvite(@Query("account") account: String): ResolveInviteResponse
    @JsonClass(generateAdapter = true)
    data class ResolveInviteResponse(val inviteKey: String)

    @GET("identity")
    suspend fun resolveIdentity(@Query("publicKey") publicKey: String): ResolveIdentityResponse
    @JsonClass(generateAdapter = true)
    data class ResolveIdentityResponse(val cacao: Cacao)

    @Headers("Content-Type: application/json")
    @POST("identity")
    suspend fun registerIdentity(@Body body: RegisterIdentityBody)
    @JsonClass(generateAdapter = true)
    data class RegisterIdentityBody(val cacao: Cacao)

}