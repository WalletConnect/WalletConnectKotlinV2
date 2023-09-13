@file:JvmSynthetic

package com.walletconnect.web3.inbox.notify.di
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.common.proxy.NotifyProxyInteractor
import com.walletconnect.web3.inbox.notify.event.NotifyEventHandler
import com.walletconnect.web3.inbox.notify.event.OnDeleteNotifyEventUseCase
import com.walletconnect.web3.inbox.notify.event.OnMessageNotifyEventUseCase
import com.walletconnect.web3.inbox.notify.event.OnSubscriptionNotifyEventUseCase
import com.walletconnect.web3.inbox.notify.event.OnSubscriptionsChangedNotifyEventUseCase
import com.walletconnect.web3.inbox.notify.event.OnUpdateNotifyEventUseCase
import com.walletconnect.web3.inbox.notify.request.DeleteNotifyMessageRequestUseCase
import com.walletconnect.web3.inbox.notify.request.DeleteSubscriptionRequestUseCase
import com.walletconnect.web3.inbox.notify.request.GetActiveSubscriptionsRequestUseCase
import com.walletconnect.web3.inbox.notify.request.GetMessageHistoryRequestUseCase
import com.walletconnect.web3.inbox.notify.request.NotifyProxyRequestHandler
import com.walletconnect.web3.inbox.notify.request.RegisterRequestUseCase
import com.walletconnect.web3.inbox.notify.request.SubscribeRequestUseCase
import com.walletconnect.web3.inbox.notify.request.UpdateRequestUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun notifyProxyModule(
    notifyClient: NotifyInterface,
    onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    account: AccountId
) = module {

    single { NotifyProxyInteractor(get(), get()) }

    single { GetActiveSubscriptionsRequestUseCase(notifyClient = notifyClient, account = account, proxyInteractor = get()) }
    single { SubscribeRequestUseCase(notifyClient = notifyClient, proxyInteractor = get()) }
    single { UpdateRequestUseCase(notifyClient = notifyClient, proxyInteractor = get()) }
    single { DeleteSubscriptionRequestUseCase(notifyClient = notifyClient, proxyInteractor = get()) }
    single { GetMessageHistoryRequestUseCase(notifyClient = notifyClient, proxyInteractor = get()) }
    single { DeleteNotifyMessageRequestUseCase(notifyClient = notifyClient, proxyInteractor = get()) }
    single { RegisterRequestUseCase(notifyClient = notifyClient, proxyInteractor = get(), onSign = onSign) }

    single { OnMessageNotifyEventUseCase(proxyInteractor = get()) }
    single { OnDeleteNotifyEventUseCase(proxyInteractor = get()) }
    single { OnSubscriptionNotifyEventUseCase(proxyInteractor = get()) }
    single { OnSubscriptionsChangedNotifyEventUseCase(proxyInteractor = get()) }
    single { OnUpdateNotifyEventUseCase(proxyInteractor = get()) }

    single { NotifyEventHandler(
        logger = get(),
        onSubscriptionNotifyEventUseCase = get(),
        onUpdateNotifyEventUseCase = get(),
        onDeleteNotifyEventUseCase = get(),
        onMessageNotifyEventUseCase = get(),
        onSubscriptionsChangedNotifyEventUseCase = get()
    ) }

    single { NotifyProxyRequestHandler(
        subscribeRequestUseCase = get(),
        updateRequestUseCase = get(),
        deleteSubscriptionRequestUseCase = get(),
        getActiveSubscriptionsRequestUseCase = get(),
        getMessageHistoryRequestUseCase = get(),
        deleteNotifyMessageRequestUseCase = get(),
        registerRequestUseCase = get(),
        logger = get()
    ) }
}
