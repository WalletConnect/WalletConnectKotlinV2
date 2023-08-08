package com.walletconnect.android.test.client

import com.walletconnect.android.BuildConfig
import com.walletconnect.android.history.network.model.messages.Direction
import com.walletconnect.android.history.network.model.messages.MessagesParams
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.utils.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.android.test.activity.WCInstrumentedActivityScenario
import com.walletconnect.android.test.json_rpc.TestHistoryParams
import com.walletconnect.android.test.json_rpc.TestHistoryRPC
import com.walletconnect.android.test.utils.TestClient
import com.walletconnect.android.test.utils.globalOnError
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Ttl
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

class HistoryInstrumentedAndroidTest {

    @get:Rule
    val scenarioExtension = WCInstrumentedActivityScenario()

    @Test
    fun registerTagsByPrimaryClient() {
        Timber.d("registerTagsByPrimaryClient: start")
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            TestClient.Primary.History.registerTags(
                listOf(Tags.UNSUPPORTED_METHOD),
                onSuccess = { scenarioExtension.closeAsSuccess() },
                onError = ::globalOnError
            )
        }
    }

    @Test
    fun registerTagsBySecondaryClient() {
        Timber.d("registerTagsBySecondaryClient: start")
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            TestClient.Secondary.History.registerTags(
                listOf(Tags.UNSUPPORTED_METHOD),
                onSuccess = { scenarioExtension.closeAsSuccess() },
                onError = ::globalOnError
            )
        }
    }

    @Test
    fun getMessagesBySecondaryClient() {
        Timber.d("getMessagesBySecondaryClient: start")
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { testScope ->
            TestClient.Primary.History.registerTags(
                listOf(Tags.UNSUPPORTED_METHOD),
                onError = ::globalOnError,
                onSuccess = {
                    val primaryPublicKey: PublicKey = TestClient.Primary.keyManagementRepository.generateAndStoreX25519KeyPair()
                    val secondaryPublicKey: PublicKey = TestClient.Secondary.keyManagementRepository.generateAndStoreX25519KeyPair()

                    val primarySymmetricKey = TestClient.Primary.keyManagementRepository.generateSymmetricKeyFromKeyAgreement(primaryPublicKey, secondaryPublicKey)
                    val secondarySymmetricKey = TestClient.Secondary.keyManagementRepository.generateSymmetricKeyFromKeyAgreement(secondaryPublicKey, primaryPublicKey)
                    assertEquals(primarySymmetricKey, secondarySymmetricKey)

                    val topic = TestClient.Primary.keyManagementRepository.getTopicFromKey(primarySymmetricKey)
                    TestClient.Primary.keyManagementRepository.setKey(primarySymmetricKey, topic.value)
                    TestClient.Secondary.keyManagementRepository.setKey(secondarySymmetricKey, topic.value)


                    val dummyParams = TestHistoryParams()
                    val dummyPayload = TestHistoryRPC(params = dummyParams)

                    Timber.d("TestClient.Primary.jsonRpcInteractor.publishJsonRpcRequest")
                    TestClient.Primary.jsonRpcInteractor.publishJsonRpcRequest(
                        topic,
                        IrnParams(Tags.UNSUPPORTED_METHOD, Ttl(FIVE_MINUTES_IN_SECONDS)),
                        dummyPayload,
                        onFailure = ::globalOnError,
                        onSuccess = {
                            Timber.d("TestClient.Primary.jsonRpcInteractor onSuccess")

                            // Let History Server receive the webhook and store
                            runBlocking { delay(1.seconds) }

                            testScope.launch {
                                TestClient.Secondary.History.getAllMessages(
                                    MessagesParams(topic.value, null, 5, Direction.BACKWARD),
                                    onSuccess = { messages ->
                                        assertEquals(1, messages.size)
                                        scenarioExtension.closeAsSuccess()
                                    },
                                    onError = ::globalOnError
                                )
                            }
                        }
                    )
                },
            )
        }
    }

    @Test
    fun getMessagesBySecondaryClientTriggersEvents() {
        Timber.d("getMessagesBySecondaryClientTriggersEvents: start")
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { testScope ->
            TestClient.Primary.History.registerTags(
                listOf(Tags.UNSUPPORTED_METHOD),
                onError = ::globalOnError,
                onSuccess = {
                    val primaryPublicKey: PublicKey = TestClient.Primary.keyManagementRepository.generateAndStoreX25519KeyPair()
                    val secondaryPublicKey: PublicKey = TestClient.Secondary.keyManagementRepository.generateAndStoreX25519KeyPair()

                    val primarySymmetricKey = TestClient.Primary.keyManagementRepository.generateSymmetricKeyFromKeyAgreement(primaryPublicKey, secondaryPublicKey)
                    val secondarySymmetricKey = TestClient.Secondary.keyManagementRepository.generateSymmetricKeyFromKeyAgreement(secondaryPublicKey, primaryPublicKey)
                    assertEquals(primarySymmetricKey, secondarySymmetricKey)

                    val topic = TestClient.Primary.keyManagementRepository.getTopicFromKey(primarySymmetricKey)
                    TestClient.Primary.keyManagementRepository.setKey(primarySymmetricKey, topic.value)
                    TestClient.Secondary.keyManagementRepository.setKey(secondarySymmetricKey, topic.value)


                    val dummyParams = TestHistoryParams()
                    val dummyPayload = TestHistoryRPC(params = dummyParams)

                    Timber.d("TestClient.Primary.jsonRpcInteractor.publishJsonRpcRequest")


                    val collectJsonRpcRequests: Job = TestClient.Secondary.jsonRpcInteractor.clientSyncJsonRpc
                        .filter { request -> request.params is TestHistoryParams }
                        .onEach { request ->
                            when (val params = request.params) {
                                is TestHistoryParams -> {
                                    Timber.d("getMessagesBySecondaryClientTriggersEvents finish")
                                    scenarioExtension.closeAsSuccess()
                                }
                            }
                        }.launchIn(testScope)

                    TestClient.Primary.jsonRpcInteractor.publishJsonRpcRequest(
                        topic,
                        IrnParams(Tags.UNSUPPORTED_METHOD, Ttl(FIVE_MINUTES_IN_SECONDS)),
                        dummyPayload,
                        onFailure = ::globalOnError,
                        onSuccess = {
                            Timber.d("TestClient.Primary.jsonRpcInteractor onSuccess")

                            runBlocking { delay(1000) }

                            testScope.launch {
                                TestClient.Secondary.History.getAllMessages(
                                    MessagesParams(topic.value, null, 5, Direction.BACKWARD),
                                    onSuccess = { messages -> assertEquals(1, messages.size) },
                                    onError = ::globalOnError
                                )
                            }
                        }
                    )
                },
            )
        }
    }
}