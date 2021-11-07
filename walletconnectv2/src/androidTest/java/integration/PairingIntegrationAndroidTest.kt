package integration

import android.app.Application
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.walletconnect.walletconnectv2.common.AppMetaData
import org.walletconnect.walletconnectv2.engine.EngineInteractor
import java.time.Duration

//@RunWith(AndroidJUnit4::class)
class PairingIntegrationAndroidTest {

    @Test
    fun pairTest() {
        Log.e("Talha", "Starting")
        runBlocking {
            val engine = EngineInteractor()
            val app: Application = ApplicationProvider.getApplicationContext()
            val metaData = AppMetaData()
            engine.initialize(EngineInteractor.EngineFactory(true, "relay.walletconnect.org", "", true, app, metaData))

            val uri =
                "wc:dbb27984971dede7a67f60a6e2cd7fdb0a81eb830c760bc067bdfaa14b32d5d0@2?controller=false&publicKey=d2cba98de92fc8d392d9034035e81086690c8d8dab773c22da86bda1048ce042&relay=%7B%22protocol%22%3A%22waku%22%7D"
            engine.pair(uri)

            launch {
                try {
                    withTimeout(Duration.ofMinutes(1).toMillis()) {
                        val subscribeDeferred = async(Dispatchers.IO) {
//                            engine.subscribeAcknowledgement.filterNotNull().collect {
//                                println("Subscribe Acknowledgement $it")
//                                assert(it.result.id.isNotBlank())
//                            }
                            supervisorScope {
//                                Log.e("Talha", "inside")
//                                assert()
                            }
                        }

                        val pairDeferred = async(Dispatchers.IO) {
                            engine.publishAcknowledgement.filterNotNull().collect {
                                Log.e("Talha", "Publish Acknowledgement: $it")
                                assert(it.result)
                            }
                        }

                        val subscriptionDeferred = async(Dispatchers.IO) {
                            engine.subscriptionRequest.collect {
                                println("Subscription Request $it")
                            }
                        }

                        val sessionProposalDeferred = async(Dispatchers.IO) {
                            engine.sessionProposal.collect {
                                println("Session Proposal: $it")
                            }
                        }

                        listOf(
                            pairDeferred,
                            subscribeDeferred,
                            subscriptionDeferred,
                            sessionProposalDeferred
                        ).awaitAll()
                    }
                } catch (timeoutException: TimeoutCancellationException) {
                    Assert.fail("timed out")
                }
            }
        }
    }
//    approveSessionTest()
}


//fun approveSessionTest() {
//    val job = SupervisorJob()
//    val scope = CoroutineScope(job + Dispatchers.IO)
//    val engine = EngineInteractor(true, "relay.walletconnect.org")
//
//    engine.approve(
//        Session.Proposal(
//            topic = Topic("69bba8737e4c7d8715b0ea92fe044ba291a359c24a3cde3e240bc8ec81fa0757"),
//            relay = RelayProtocolOptions(protocol = "waku"),
//            proposer = SessionProposer(
//                publicKey = "fa75874568a6f347229c5936f34ac7e2117f5233e13e3e418332687acd56382c",
//                controller = false, null
//            ),
//            signal = SessionSignal(params = SessionSignal.Params(topic = Topic("15a66762d0a589a2330c73a627a8b83668f3b1eb7f172da1bedf045e09108aec"))),
//            permissions = SessionProposedPermissions(
//                blockchain = SessionProposedPermissions.Blockchain(chains = listOf("eip155:42")),
//                jsonRpc = SessionProposedPermissions.JsonRpc(
//                    methods = listOf(
//                        "eth_sendTransaction",
//                        "personal_sign",
//                        "eth_signTypedData"
//                    )
//                ),
//                notifications = SessionProposedPermissions.Notifications(types = listOf())
//            ),
//            ttl = Ttl(604800)
//        )
//    )
//
//    scope.launch {
//        try {
//
//            withTimeout(Duration.ofMinutes(1).toMillis()) {
//
//                val pairDeferred = async(Dispatchers.IO) {
//                    engine.publishAcknowledgement.collect {
//                        println("Publish Acknowledgement: $it")
//                        require(it.result) {
//                            "Acknowledgement from Relay returned false"
//                        }
//                    }
//                }
//
//                pairDeferred.await()
//            }
//        } catch (timeoutException: TimeoutCancellationException) {
//            println("timed out")
//            exitProcess(0)
//        }
//    }
//}