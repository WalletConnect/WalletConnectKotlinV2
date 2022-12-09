package com.walletconnect.push.wallet.client

import com.walletconnect.push.common.Push

class WalletProtocol: WalletInterface {

    companion object {
        val instance = WalletProtocol()
    }

    override fun initialize() {
        TODO("Not yet implemented")
    }

    override fun approve(params: Push.Wallet.Params.Approve, onSuccess: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun reject(params: Push.Wallet.Params.Reject, onSuccess: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getActiveSubscriptions(): Map<String, Push.Wallet.Model.Subscription> {
        TODO("Not yet implemented")
    }

    override fun delete(params: Push.Wallet.Params.Delete) {
        TODO("Not yet implemented")
    }

    override fun decryptMessage(params: Push.Wallet.Params.DecryptMessage, onSuccess: (Push.Wallet.Model.Message) -> Unit) {
        TODO("Not yet implemented")
    }
}