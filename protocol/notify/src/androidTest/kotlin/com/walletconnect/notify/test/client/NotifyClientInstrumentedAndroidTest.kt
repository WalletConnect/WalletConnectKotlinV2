package com.walletconnect.notify.test.client

import androidx.core.net.toUri
import com.walletconnect.notify.BuildConfig
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.notify.test.scenario.ClientInstrumentedActivityScenario
import com.walletconnect.notify.test.utils.TestClient
import com.walletconnect.notify.test.utils.primary.PrimaryNotifyClient
import com.walletconnect.notify.test.utils.primary.PrimaryNotifyDelegate
import com.walletconnect.notify.test.utils.secondary.SecondaryNotifyClient
import com.walletconnect.notify.test.utils.secondary.SecondaryNotifyDelegate
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import timber.log.Timber

class NotifyClientInstrumentedAndroidTest {
    @get:Rule
    val scenarioExtension = ClientInstrumentedActivityScenario()

    private fun setDelegates(primaryNotifyDelegate: NotifyInterface.Delegate, secondaryNotifyDelegate: NotifyInterface.Delegate) {
        PrimaryNotifyClient.setDelegate(primaryNotifyDelegate)
        SecondaryNotifyClient.setDelegate(secondaryNotifyDelegate)
    }

    @Test
    fun deleteSubscription() {
        setDelegates(object : PrimaryNotifyDelegate() {
            override fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged) {

                Timber.d("deleteSubscription: primary - deleteSubscribe start")
                if (subscriptionsChanged.subscriptions.isNotEmpty()) {

                    PrimaryNotifyClient.deleteSubscription(Notify.Params.DeleteSubscription(subscriptionsChanged.subscriptions.first().topic), {
                        Timber.d("deleteSubscription: primary - deleteSubscribe failure: ${it.throwable}")
                    })
                    runBlocking { delay(2000) }

                }

                scenarioExtension.closeAsSuccess()
            }
        }, SecondaryNotifyDelegate())

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {}
    }

    @Test
    fun areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage() {
        Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: start")
        var countPrimaryReceivedResponses = 0
        var countSecondaryReceivedResponses = 0
        var didPrimaryReceiveSubscriptions = false
        var didSecondaryReceiveSubscriptions = false

        setDelegates(object : PrimaryNotifyDelegate() {
            override fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged) {
                countPrimaryReceivedResponses++
                Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - response($countPrimaryReceivedResponses)")

                if (countPrimaryReceivedResponses > 1 && subscriptionsChanged.subscriptions.isNotEmpty()) {
                    didPrimaryReceiveSubscriptions = true
                }

                if(didPrimaryReceiveSubscriptions && didSecondaryReceiveSubscriptions){
                    scenarioExtension.closeAsSuccess()
                }
                Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - response(${subscriptionsChanged.subscriptions})")


                if (subscriptionsChanged.subscriptions.isEmpty()) {
                    Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - subscribe start")

                    PrimaryNotifyClient.subscribe(Notify.Params.Subscribe("https://gm.walletconnect.com".toUri(), TestClient.caip10account), {
                        Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - subscribe success")
                    }, {
                        Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - subscribe failure: ${it.throwable}")
                    })
                } else if (countPrimaryReceivedResponses < 2) {
                    Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - deleteSubscribe start")

                    PrimaryNotifyClient.deleteSubscription(Notify.Params.DeleteSubscription(subscriptionsChanged.subscriptions.first().topic), {
                        Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - deleteSubscribe failure: ${it.throwable}")
                    })
                }
            }
        }, object : SecondaryNotifyDelegate() {
            override fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged) {
                countSecondaryReceivedResponses++
                Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: secondary - response($countSecondaryReceivedResponses)")
                Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: secondary - response(${subscriptionsChanged.subscriptions})")

                if (countSecondaryReceivedResponses > 1 && subscriptionsChanged.subscriptions.isNotEmpty()) {
                    didSecondaryReceiveSubscriptions = true
                }

                if(didPrimaryReceiveSubscriptions && didSecondaryReceiveSubscriptions){
                    scenarioExtension.closeAsSuccess()
                }
            }
        })

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {}
    }
}