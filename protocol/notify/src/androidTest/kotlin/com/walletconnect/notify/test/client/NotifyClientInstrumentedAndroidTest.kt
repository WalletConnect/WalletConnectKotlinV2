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
    private val notifyUrl = "https://notify.walletconnect.com/${BuildConfig.NOTIFY_INTEGRATION_TESTS_PROJECT_ID}/notify"
    private fun createBody(): RequestBody {
        val jsonMediaType: MediaType = mediaTypeString.toMediaType()
        val postBody =
            """{
                "notification": {
                    "body": "This was send from our IT",
                    "title": "Kotlin Notify API IT!",
                    "icon": "https://github.com/WalletConnect/walletconnect-assets/blob/master/Icon/Black/Icon.png?raw=true",
                    "url": "https://specs.walletconnect.com",
                    "type": "f173f231-a45c-4dc0-aa5d-956eb04f7360"
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
            .add("Authorization", "Bearer ${BuildConfig.NOTIFY_INTEGRATION_TESTS_SECRET}")
            .build()
    }

    fun sendTestNotification(): Boolean {
        val request: Request = Request.Builder().url(notifyUrl).headers(createHeaders()).post(createBody()).build()
        val response: Response = OkHttpClient().newCall(request).execute()
        val responseString = response.body?.string() ?: throw Exception("Response body is null")

        Timber.d("sendTestNotification: $responseString, ${getResponseResult(responseString)}")
        return getResponseResult(responseString)[0] == TestClient.caip10account
    }

    private fun getResponseResult(payload: String): JSONArray {
        return JSONObject(payload.trimIndent()).get("sent") as JSONArray
    }

    private fun setDelegates(primaryNotifyDelegate: NotifyInterface.Delegate, secondaryNotifyDelegate: NotifyInterface.Delegate) {
        PrimaryNotifyClient.setDelegate(primaryNotifyDelegate)
        SecondaryNotifyClient.setDelegate(secondaryNotifyDelegate)
    }

    @Test
    fun areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage() {
        var countPrimaryReceivedResponses = 0
        var countSecondaryReceivedResponses = 0
        var didPrimaryReceiveSubscriptions = false
        var didSecondaryReceiveSubscriptions = false
        var wasMessageSent = false
        var didPrimaryReceiveMessage = false
        var didSecondaryReceiveMessage = false

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: start")
            setDelegates(object : PrimaryNotifyDelegate() {
                override fun onNotifyNotification(notifyNotification: Notify.Event.Notification) {
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

                        PrimaryNotifyClient.subscribe(Notify.Params.Subscribe("https://wc-notify-swift-integration-tests-prod.pages.dev".toUri(), TestClient.caip10account), {
                            Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - subscribe success")
                        }, {
                            Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - subscribe failure: ${it.throwable}")
                        })
                    } else if (countPrimaryReceivedResponses < 2) {
                        Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - deleteSubscribe start")

                        runBlocking { delay(1000) }
                        PrimaryNotifyClient.deleteSubscription(Notify.Params.DeleteSubscription(subscriptionsChanged.subscriptions.first().topic), onSuccess = {
                            scenarioExtension.closeAsSuccess()
                        }, onError = {
                            Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: primary - deleteSubscribe failure: ${it.throwable}")
                        })
                    }
                }
            }, object : SecondaryNotifyDelegate() {

                override fun onNotifyNotification(notifyNotification: Notify.Event.Notification) {
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
            Timber.d("areTwoClientsInSyncAfterHavingSubscriptionAndReceivingMessage: delegates set")
            scenarioExtension.initializeClients()
        }
    }
}