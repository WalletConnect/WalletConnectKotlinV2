package org.walletconnect.example.wallet

import android.app.Application
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.walletconnect.example.R
import org.walletconnect.example.wallet.ui.*
import org.walletconnect.walletconnectv2.WalletConnectClient
import org.walletconnect.walletconnectv2.WalletConnectClientListeners
import org.walletconnect.walletconnectv2.outofband.client.ClientTypes

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    init {
//        Initialize SDK with proper parameters
//        val initParams = ClientTypes.InitialParams(
//            useTls = true,
//            hostName = "relay.walletconnect.com",
//            apiKey = "",
//            isController = true
//        )
//        WalletConnectClient.initialize(initParams)
    }

    private var _eventFlow = MutableSharedFlow<WalletUiEvent>()
    val eventFlow = _eventFlow.asLiveData()

    val activeSessions: MutableList<Session> = mutableListOf()

    fun pair(uri: String) {
// Call pair method from SDK and setup callback for session proposal event. Once it's received show session proposal dialog
//        val sessionProposalListener = WalletConnectClientListeners.Session { sessionProposal ->
//
//        }
//        val pairingParams = ClientTypes.PairParams(uri = uri)
//         WalletConnectClient.pair(pairingParams, sessionProposalListener)



        //mocked session proposal
        val sessionProposal = SessionProposal(
            name = "WalletConnect",
            icon = R.drawable.ic_walletconnect_circle_blue,
            uri = "app.walletconnect.org",
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut eu accumsan nunc. Cras luctus, ipsum at tempor vulputate, metus purus mollis ex, ut maximus tellus lectus non nisl. Duis eu diam sollicitudin, bibendum enim ut, elementum erat.",
            chains = listOf("Ethereum Kovan", "BSC Mainnet", "Fantom Opera"),
            methods = listOf("personal_sign", "eth_sendTransaction", "eth_signedTypedData")
        )

        viewModelScope.launch {
            _eventFlow.emit(ShowSessionProposalDialog(sessionProposal))
        }
    }

    fun approve() {
//        Call approve method from SDK to approve session proposal
//        WalletConnectClient.approve()

        val session = Session(
            name = "WalletConnect",
            uri = "app.walletconnect.org",
            icon = R.drawable.ic_walletconnect_circle_blue
        )

        activeSessions += session

        //call approve() session method from SDK
        viewModelScope.launch {
            _eventFlow.emit(UpdateActiveSessions(activeSessions))
        }
    }

    fun reject() {
        //call reject() session method from SDK
    }
}