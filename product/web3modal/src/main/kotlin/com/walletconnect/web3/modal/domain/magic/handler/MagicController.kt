package com.walletconnect.web3.modal.domain.magic.handler

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.domain.magic.MagicEngine
import com.walletconnect.web3.modal.domain.magic.model.ConnectEmail
import com.walletconnect.web3.modal.domain.magic.model.ConnectOtp
import com.walletconnect.web3.modal.domain.magic.model.GetUser
import com.walletconnect.web3.modal.domain.magic.model.IsConnected
import com.walletconnect.web3.modal.domain.magic.model.MagicEvent
import com.walletconnect.web3.modal.domain.magic.model.RpcRequest
import com.walletconnect.web3.modal.domain.magic.model.SignOut
import com.walletconnect.web3.modal.domain.magic.model.SwitchNetwork

internal class MagicController(
//    private val magicEngine: MagicEngine
//    private val context: Context,
    private val logger: Logger,
    private val appMetaData: AppMetaData,
    private val projectId: ProjectId,
) : MagicControllerInterface {

    private lateinit var magicEngine: MagicEngine

//    init {
//        magicEngine = MagicEngine(
//            logger = logger,
//            appMetaData = appMetaData,
//            projectId = projectId
//        )
//    }

    override fun init() {
        println("kobe: init Magic Controller")
        magicEngine = MagicEngine(
            logger = logger,
            appMetaData = appMetaData,
            projectId = projectId
        )

    }

    override suspend fun checkConnection(): Result<MagicEvent.IsConnectResult> = runCatching {
        magicEngine.sendMessage<MagicEvent.IsConnectResult>(IsConnected)
    }

    override suspend fun connect(email: String): Result<MagicEvent.ConnectEmailResult> = runCatching {
        magicEngine.sendMessage<MagicEvent.ConnectEmailResult>(ConnectEmail(email))
    }

    override suspend fun connectOTP(otp: String): Result<MagicEvent.ConnectOTPResult> = runCatching {
        magicEngine.sendMessage<MagicEvent.ConnectOTPResult>(ConnectOtp(otp))
    }

    override suspend fun getUser(chainId: String?): Result<MagicEvent.GetUserResult> = runCatching {
        magicEngine.sendMessage<MagicEvent.GetUserResult>(GetUser(chainId))
    }

    override suspend fun switchNetwork(chainId: String): Result<MagicEvent.SwitchNetworkResult> = runCatching {
        magicEngine.sendMessage<MagicEvent.SwitchNetworkResult>(SwitchNetwork(chainId))
    }

    override suspend fun request(method: String, params: String): Result<MagicEvent.RpcRequestResult> = runCatching {
        magicEngine.sendMessage<MagicEvent.RpcRequestResult>(RpcRequest(method, params))
    }

    override suspend fun signOut(): Result<MagicEvent.SignOutSuccess> = runCatching {
        magicEngine.sendMessage<MagicEvent.SignOutSuccess>(SignOut)
    }
}
