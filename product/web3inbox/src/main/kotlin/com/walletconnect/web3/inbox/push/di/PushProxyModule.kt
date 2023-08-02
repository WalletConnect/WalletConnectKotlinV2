@file:JvmSynthetic

package com.walletconnect.web3.inbox.push.di

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.push.event.OnDeleteNotifyEventUseCase
import com.walletconnect.web3.inbox.push.event.OnMessageNotifyEventUseCase
import com.walletconnect.web3.inbox.push.event.OnSubscriptionNotifyEventUseCase
import com.walletconnect.web3.inbox.push.event.OnSyncUpdateNotifyEventUseCase
import com.walletconnect.web3.inbox.push.event.OnUpdateNotifyEventUseCase
import com.walletconnect.web3.inbox.push.event.NotifyEventHandler
import com.walletconnect.web3.inbox.push.request.DeleteNotifyMessageRequestUseCase
import com.walletconnect.web3.inbox.push.request.DeleteSubscriptionRequestUseCase
import com.walletconnect.web3.inbox.push.request.EnableSyncRequestUseCase
import com.walletconnect.web3.inbox.push.request.GetActiveSubscriptionsRequestUseCase
import com.walletconnect.web3.inbox.push.request.GetMessageHistoryRequestUseCase
import com.walletconnect.web3.inbox.push.request.NotifyProxyRequestHandler
import com.walletconnect.web3.inbox.push.request.SubscribeRequestUseCase
import com.walletconnect.web3.inbox.push.request.UpdateRequestUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun notifyProxyModule(
    notifyClient: NotifyInterface,
    onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    account: AccountId
) = module {

    single { NotifyProxyInteractor(get(), get()) }

    single { GetActiveSubscriptionsRequestUseCase(notifyClient, account, get()) }
    single { SubscribeRequestUseCase(notifyClient = notifyClient, onSign = onSign, proxyInteractor = get()) }
    single { UpdateRequestUseCase(notifyClient, get()) }
    single { DeleteSubscriptionRequestUseCase(notifyClient, get()) }
    single { GetMessageHistoryRequestUseCase(notifyClient, get()) }
    single { DeleteNotifyMessageRequestUseCase(notifyClient, get()) }
    single { EnableSyncRequestUseCase(notifyClient, get(), onSign) }

    single { OnMessageNotifyEventUseCase(proxyInteractor = get()) }
    single { OnDeleteNotifyEventUseCase(proxyInteractor = get()) }
    single { OnSyncUpdateNotifyEventUseCase(proxyInteractor = get()) }
    single { OnSubscriptionNotifyEventUseCase(proxyInteractor = get()) }
    single { OnUpdateNotifyEventUseCase(proxyInteractor = get()) }

    single { NotifyEventHandler(
        logger = get(),
        onSubscriptionNotifyEventUseCase = get(),
        onUpdateNotifyEventUseCase = get(),
        onDeleteNotifyEventUseCase = get(),
        onMessageNotifyEventUseCase = get()
    ) }

    single { NotifyProxyRequestHandler(
        subscribeRequestUseCase = get(),
        updateRequestUseCase = get(),
        deleteSubscriptionRequestUseCase = get(),
        getActiveSubscriptionsRequestUseCase = get(),
        getMessageHistoryRequestUseCase = get(),
        deletePushMessageRequestUseCase = get(),
        enableSyncRequestUseCase = get()
    ) }
}
