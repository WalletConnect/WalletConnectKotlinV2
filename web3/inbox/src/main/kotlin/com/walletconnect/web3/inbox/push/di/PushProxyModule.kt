@file:JvmSynthetic

package com.walletconnect.web3.inbox.push.di

import com.walletconnect.push.wallet.client.PushWalletInterface
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.push.event.*
import com.walletconnect.web3.inbox.push.request.*
import org.koin.dsl.module

@JvmSynthetic
internal fun pushProxyModule(
    pushWalletClient: PushWalletInterface,
    onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    onPageFinished: () -> Unit,
) = module {

    single { PushProxyInteractor(get(), get()) }

    single { GetActiveSubscriptionsRequestUseCase(pushWalletClient, get()) }
    single { ApproveRequestUseCase(pushWalletClient, get(), onSign) }
    single { RejectRequestUseCase(pushWalletClient, get()) }
    single { SubscribeRequestUseCase(pushWalletClient, onSign, get()) }
    single { UpdateRequestUseCase(pushWalletClient, get()) }
    single { DeleteSubscriptionRequestUseCase(pushWalletClient, get()) }
    single { GetMessageHistoryRequestUseCase(pushWalletClient, get()) }
    single { DeletePushMessageRequestUseCase(pushWalletClient, get()) }

    single { OnRequestPushEventUseCase(get()) }
    single { OnMessagePushEventUseCase(get()) }
    single { OnDeletePushEventUseCase(get()) }
    single { OnSyncUpdatePushEventUseCase(get()) }
    single { OnSubscriptionPushEventUseCase(get()) }
    single { OnUpdatePushEventUseCase(get()) }

    single { PushEventHandler(get(), get(), get(), get(), get(), get()) }

    single { PushProxyRequestHandler(get(), get(), get(), get(), get(), get(), get(), get()) }
}
