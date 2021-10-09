package integration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.walletconnect.walletconnectv2.engine.EngineInteractor

fun main() {
    val job = SupervisorJob()
    val scope = CoroutineScope(job + Dispatchers.IO)
    val engine = EngineInteractor("staging.walletconnect.org?apiKey=c4f79cc821944d9680842e34466bfbd")
    val publishResponse = engine.pairingResponse
    val uri =
        "wc:3b96370416cb1a17d13f322498f949e9f6d6277674d069b97875a5812af9cc62@2?controller=false&publicKey=685fbc524d9aa33d203919c96dcac2295b5145616dca26d839bbc13b1bf95d77&relay=%7B%22protocol%22%3A%22waku%22%7D"

    scope.launch {
        engine.pair(uri)

        publishResponse.collect {
            require(it.result) {
                "Response from Relay returned false"
            }
        }
    }
}