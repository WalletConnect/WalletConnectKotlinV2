package com.walletconnect.sample.modal.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.sample.common.getEthSendTransaction
import com.walletconnect.sample.common.getEthSignTypedData
import com.walletconnect.sample.common.getPersonalSignBody
import com.walletconnect.sample.common.ui.commons.BlueButton
import com.walletconnect.sample.modal.ModalSampleDelegate
import com.walletconnect.sample.modal.common.openAlertDialog
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.client.models.request.Request
import com.walletconnect.web3.modal.client.models.request.SentRequestResult
import com.walletconnect.web3.modal.ui.Web3ModalTheme
import com.walletconnect.web3.modal.ui.components.button.AccountButtonType
import com.walletconnect.web3.modal.ui.components.button.NetworkButton
import com.walletconnect.web3.modal.ui.components.button.Web3Button
import com.walletconnect.web3.modal.ui.components.button.rememberWeb3ModalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LabScreen(
    navController: NavController
) {
    val web3ModalState = rememberWeb3ModalState(navController = navController)
    val isConnected by web3ModalState.isConnected.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        ModalSampleDelegate.wcEventModels.collect { event ->
            when(event) {
                is Modal.Model.SessionRequestResponse -> {
                    when(event.result) {
                        is Modal.Model.JsonRpcResponse.JsonRpcError -> {
                            val error = event.result as Modal.Model.JsonRpcResponse.JsonRpcError
                            navController.openAlertDialog("Error Message: ${error.message}\n Error Code: ${error.code}")
                        }
                        is Modal.Model.JsonRpcResponse.JsonRpcResult -> navController.openAlertDialog((event.result as Modal.Model.JsonRpcResponse.JsonRpcResult).result)
                    }
                }
                is Modal.Model.Error -> { navController.openAlertDialog(event.throwable.localizedMessage ?:  "Something went wrong") }
                else -> Unit
            }
        }
    }

    Web3ModalTheme(
        mode = Web3ModalTheme.Mode.AUTO
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Web3Button(state = web3ModalState, accountButtonType = AccountButtonType.MIXED) }
            item { NetworkButton(state = web3ModalState) }
            if (isConnected) {
                Web3Modal.getAccount()?.let { session ->
                    val account = session.address
                    val onError: (Throwable) -> Unit = {
                        coroutineScope.launch(Dispatchers.Main) {
                            Toast.makeText(context, it.localizedMessage ?: "Error trying to send request", Toast.LENGTH_SHORT).show()
                        }
                    }
                    item { BlueButton(text = "Personal sign", onClick = { sendPersonalSignRequest(account, {}, onError) }) }
                    item { BlueButton(text = "Eth send transaction", onClick = { sendEthSendTransactionRequest(account, {}, onError) }) }
                    item { BlueButton(text = "Eth sign typed data", onClick = { sendEthSignTypedDataRequest(account, {}, onError) }) }
                }
            }
        }
    }
}

private fun sendPersonalSignRequest(
    account: String,
    onSuccess: (SentRequestResult) -> Unit,
    onError: (Throwable) -> Unit
) {
    Web3Modal.request(
        request = Request("personal_sign", getPersonalSignBody(account)),
        onSuccess = onSuccess,
        onError = onError,
    )
}

private fun sendEthSendTransactionRequest(
    account: String,
    onSuccess: (SentRequestResult) -> Unit,
    onError: (Throwable) -> Unit
) {
    Web3Modal.request(
        request = Request("eth_sendTransaction", getEthSendTransaction(account)),
        onSuccess = onSuccess,
        onError = onError,
    )
}

private fun sendEthSignTypedDataRequest(
    account: String,
    onSuccess: (SentRequestResult) -> Unit,
    onError: (Throwable) -> Unit
) {
    Web3Modal.request(
        request = Request("eth_signTypedData", getEthSignTypedData(account)),
        onSuccess = onSuccess,
        onError = onError,
    )
}
