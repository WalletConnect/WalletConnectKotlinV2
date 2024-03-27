package com.walletconnect.web3.modal.domain.magic.handler

import com.walletconnect.web3.modal.domain.magic.model.MagicEvent

internal interface MagicControllerInterface {

    fun init()

    suspend fun checkConnection(): Result<MagicEvent.IsConnectResult>

    suspend fun connect(email: String): Result<MagicEvent.ConnectEmailResult>

    suspend fun connectOTP(otp: String): Result<MagicEvent.ConnectOTPResult>

    suspend fun getUser(chainId: String?): Result<MagicEvent.GetUserResult>

    suspend fun switchNetwork(chainId: String): Result<MagicEvent.SwitchNetworkResult>

    suspend fun request(method: String, params: String): Result<MagicEvent.RpcRequestResult>

    suspend fun signOut(): Result<MagicEvent.SignOutSuccess>
}