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
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import timber.log.Timber

class NotifyClientInstrumentedAndroidTest {
    @get:Rule
    val scenarioExtension = ClientInstrumentedActivityScenario()


    private val mediaTypeString = "application/json; charset=utf-8"
    private val notifyUrl = "https://notify.walletconnect.com/${BuildConfig.PROD_GM_PROJECT_ID}/notify"
    private fun createBody(): RequestBody {
        val jsonMediaType: MediaType = mediaTypeString.toMediaType()
        val postBody =
            """{
                "notification": {
                    "body": "This was send from our IT",
                    "title": "GM from Kotlin IT!",
                    "icon": "https://images.unsplash.com/photo-1581224463294-908316338239?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=250&q=80",
                    "url": "https://gm.walletconnect.com",
                    "type": "cad9a52d-9b0f-4aed-9cca-3e9568a079f9"
                },
                "accounts": [
                    "${TestClient.caip10account}"
                ]
            }""".trimMargin()

        return postBody.toRequestBody(jsonMediaType)
    }

    private fun createHeaders(): Headers {
        return Headers.Builder()
            .add("Content-Type", "application/json")
            .add("Authorization", "Bearer ${BuildConfig.PROD_GM_SECRET}")
            .build()
    }

    fun sendTestNotification(): Boolean {
        val request: Request = Request.Builder().url(notifyUrl).headers(createHeaders()).post(createBody()).build()
        val response: Response = OkHttpClient().newCall(request).execute()
        val responseString = response.body?.string() ?: throw Exception("Response body is null")

        return getResponseResult(responseString)[0] == TestClient.caip10account
    }

    private fun getResponseResult(payload: String): JSONArray {
        return JSONObject(payload.trimIndent()).get("sent") as JSONArray
    }

    private fun setDelegates(primaryNotifyDelegate: NotifyInterface.Delegate, secondaryNotifyDelegate: NotifyInterface.Delegate) {
        PrimaryNotifyClient.setDelegate(primaryNotifyDelegate)
        SecondaryNotifyClient.setDelegate(secondaryNotifyDelegate)
    }

//    Note: This test is commented out. It's useful whenever we want to manually delete current subscription. However the areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage can handle
//    testing with existing subscription

    //    @Test
    fun deleteSubscription() {
        setDelegates(object : PrimaryNotifyDelegate() {
            override fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged) {
                Timber.d("deleteSubscription: primary - deleteSubscribe start")
                if (subscriptionsChanged.subscriptions.isNotEmpty()) {

                    PrimaryNotifyClient.deleteSubscription(Notify.Params.DeleteSubscription(subscriptionsChanged.subscriptions.first().topic),
                        onSuccess = {
                            scenarioExtension.closeAsSuccess()
                        }, onError = {
                            Timber.d("deleteSubscription: primary - deleteSubscribe failure: ${it.throwable}")
                        })

                }

            }
        }, SecondaryNotifyDelegate())

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {}
    }

    @Test //TODO this test fails without a reason
    fun areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage() {
        Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: start")
        var countPrimaryReceivedResponses = 0
        var countSecondaryReceivedResponses = 0
        var didPrimaryReceiveSubscriptions = false
        var didSecondaryReceiveSubscriptions = false
        var wasMessageSent = false
        var didPrimaryReceiveMessage = false
        var didSecondaryReceiveMessage = false

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {

            setDelegates(object : PrimaryNotifyDelegate() {
                override fun onNotifyMessage(notifyMessage: Notify.Event.Message) {
                    didPrimaryReceiveMessage = true
                    Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - message - received")
                    if (didPrimaryReceiveMessage && didSecondaryReceiveMessage && didPrimaryReceiveSubscriptions && didSecondaryReceiveSubscriptions) {
                        scenarioExtension.closeAsSuccess()
                    }
                }

                override fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged) {
                    countPrimaryReceivedResponses++

                    if (countPrimaryReceivedResponses > 1 && subscriptionsChanged.subscriptions.isNotEmpty()) {
                        didPrimaryReceiveSubscriptions = true
                    }
                    Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - response($countPrimaryReceivedResponses): $didPrimaryReceiveSubscriptions")
                    Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - response(${subscriptionsChanged.subscriptions})")

                    if (didPrimaryReceiveSubscriptions && didSecondaryReceiveSubscriptions) {
                        if (!wasMessageSent) {
                            Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - message - sent")
                            sendTestNotification()
                            wasMessageSent = true
                        }
                    }


                    if (subscriptionsChanged.subscriptions.isEmpty()) {
                        Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - subscribe start")

                        PrimaryNotifyClient.subscribe(Notify.Params.Subscribe("https://gm.walletconnect.com".toUri(), TestClient.caip10account), {
                            Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - subscribe success")
                        }, {
                            Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - subscribe failure: ${it.throwable}")
                        })
                    } else if (countPrimaryReceivedResponses < 2) {
                        Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - deleteSubscribe start")

                        PrimaryNotifyClient.deleteSubscription(Notify.Params.DeleteSubscription(subscriptionsChanged.subscriptions.first().topic), onSuccess = {
                            scenarioExtension.closeAsSuccess()
                        }, onError = {
                            Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - deleteSubscribe failure: ${it.throwable}")
                        })
                    }
                }
            }, object : SecondaryNotifyDelegate() {
                override fun onNotifyMessage(notifyMessage: Notify.Event.Message) {
                    didSecondaryReceiveMessage = true
                    Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: secondary - message - received")

                    if (didPrimaryReceiveMessage && didSecondaryReceiveMessage && didPrimaryReceiveSubscriptions && didSecondaryReceiveSubscriptions) {
                        scenarioExtension.closeAsSuccess()
                    }
                }

                override fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged) {
                    countSecondaryReceivedResponses++


                    if (countSecondaryReceivedResponses > 1 && subscriptionsChanged.subscriptions.isNotEmpty()) {
                        didSecondaryReceiveSubscriptions = true
                    }
                    Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: secondary - response($countSecondaryReceivedResponses): $didSecondaryReceiveSubscriptions")
                    Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: secondary - response(${subscriptionsChanged.subscriptions})")

                    if (didPrimaryReceiveSubscriptions && didSecondaryReceiveSubscriptions) {
                        if (!wasMessageSent) {
                            Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: secondary - message - sent")
                            sendTestNotification()
                            wasMessageSent = true
                        }
                    }
                }
            })
        }
    }
}