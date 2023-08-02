@file:JvmSynthetic

package com.walletconnect.web3.inbox.push.di

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.push.client.PushWalletInterface
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.common.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.push.event.OnDeletePushEventUseCase
import com.walletconnect.web3.inbox.push.event.OnMessagePushEventUseCase
import com.walletconnect.web3.inbox.push.event.OnSubscriptionPushEventUseCase
import com.walletconnect.web3.inbox.push.event.OnSyncUpdatePushEventUseCase
import com.walletconnect.web3.inbox.push.event.OnUpdatePushEventUseCase
import com.walletconnect.web3.inbox.push.event.PushEventHandler
import com.walletconnect.web3.inbox.push.request.DeleteNotifyMessageRequestUseCase
import com.walletconnect.web3.inbox.push.request.DeleteSubscriptionRequestUseCase
import com.walletconnect.web3.inbox.push.request.EnableSyncRequestUseCase
import com.walletconnect.web3.inbox.push.request.GetActiveSubscriptionsRequestUseCase
import com.walletconnect.web3.inbox.push.request.GetMessageHistoryRequestUseCase
import com.walletconnect.web3.inbox.push.request.PushProxyRequestHandler
import com.walletconnect.web3.inbox.push.request.SubscribeRequestUseCase
import com.walletconnect.web3.inbox.push.request.UpdateRequestUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun pushProxyModule(
    pushWalletClient: PushWalletInterface,
    onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    account: AccountId
) = module {

    single { PushProxyInteractor(get(), get()) }

    single { GetActiveSubscriptionsRequestUseCase(pushWalletClient, account, get()) }
    single { SubscribeRequestUseCase(pushWalletClient, onSign, get()) }
    single { UpdateRequestUseCase(pushWalletClient, get()) }
    single { DeleteSubscriptionRequestUseCase(pushWalletClient, get()) }
    single { GetMessageHistoryRequestUseCase(pushWalletClient, get()) }
    single { DeleteNotifyMessageRequestUseCase(pushWalletClient, get()) }
    single { EnableSyncRequestUseCase(pushWalletClient, get(), onSign) }

    single { OnMessagePushEventUseCase(get()) }
    single { OnDeletePushEventUseCase(get()) }
    single { OnSyncUpdatePushEventUseCase(get()) }
    single { OnSubscriptionPushEventUseCase(get()) }
    single { OnUpdatePushEventUseCase(get()) }

    single { PushEventHandler(get(), get(), get(), get(), get()) }

    single { PushProxyRequestHandler(get(), get(), get(), get(), get(), get(), get()) }
}
