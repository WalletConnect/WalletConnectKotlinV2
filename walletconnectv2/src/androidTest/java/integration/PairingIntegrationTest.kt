package integration

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.junit.Test
import org.junit.runner.RunWith
import org.walletconnect.walletconnectv2.common.AppMetaData
import org.walletconnect.walletconnectv2.engine.EngineInteractor
import java.time.Duration
import kotlin.system.exitProcess

@RunWith(AndroidJUnit4::class)
class PairingIntegrationTest {

    @Test
    fun pairTest() {
        val job = SupervisorJob()
        val scope = CoroutineScope(job + Dispatchers.IO)
        val engine = EngineInteractor()
        val app: Application = ApplicationProvider.getApplicationContext()
        val metaData = AppMetaData()
        engine.initialize(EngineInteractor.EngineFactory(true, "relay.walletconnect.org", "", true, app, metaData))

        val uri =
            "wc:ae6ce2015b86fcfe030a63e77bef47586107ef3ff628f4452cd05b4c33b6646f@2?controller=false&publicKey=24c953bd2ed1254f9f4316b299d35f4281e1a01f3b0faea1904b6ef344586e40&relay=%7B%22protocol%22%3A%22waku%22%7D"
        engine.pair(uri)

        scope.launch {
            try {
                withTimeout(Duration.ofMinutes(20).toMillis()) {
                    val pairDeferred = async(Dispatchers.IO) {
                        engine.publishAcknowledgement.collect {
                            println("Publish Acknowledgement: $it")
                            require(it.result) {
                                "Acknowledgement from Relay returned false"
                            }
                        }
                    }

                    val subscribeDeferred = async(Dispatchers.IO) {
                        engine.subscribeAcknowledgement.collect {
                            println("Subscribe Acknowledgement $it")
                            require(it.result.id.isNotBlank()) {
                                "Acknowledgement from Relay returned false"
                            }
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
                println("timed out")
                exitProcess(0)
            }
        }
    }
//    approveSessionTest()
//    }
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