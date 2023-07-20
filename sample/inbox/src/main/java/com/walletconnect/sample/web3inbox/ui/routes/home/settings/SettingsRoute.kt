package com.walletconnect.sample.web3inbox.ui.routes.home.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.sample.web3inbox.domain.SharedPrefStorage
import com.walletconnect.sample.web3inbox.ui.routes.navigateToSelectAccount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun SettingsRoute(navController: NavController) {

    val viewModel: SettingsViewModel = viewModel()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val wasCopied = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val account = viewModel.selectedAccount.split(":").last()
    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Text(fontSize = 40.sp, text = account.let { it.take(6) + "..." + it.takeLast(4) })
        Button(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
            onClick = {
                clipboardManager.setText(AnnotatedString(account))
                wasCopied.value = true
                coroutineScope.launch {
                    delay(2.seconds)
                    wasCopied.value = false
                }
            }) {
            Text(text = if (!wasCopied.value) "Copy address" else "Address Copied!")
        }
        Button(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
            onClick = {
                SharedPrefStorage.removeLastLoggedInAccount(context)
                navController.navigateToSelectAccount()
            }) {
            Text(text = "Log out")
        }
    }
}