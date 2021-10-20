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
        "wc:943af663b856bc90f07fd24d13c43460fe3cb48b7d1d992c5bbf3cae5989277a@2?controller=false&publicKey=e578b4aec0862606c1c3a72b5d3b636a2b52017fa6db65ac738e7f6b842b406f&relay=%7B%22protocol%22%3A%22waku%22%7D"

    scope.launch {
        engine.pair(uri)

        publishResponse.collect {
            require(it.result) {
                "Response from Relay returned false"
            }
        }
    }
}