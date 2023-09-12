package com.walletconnect.sample.web3inbox.ui.routes.select_account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.sample.web3inbox.domain.SharedPrefStorage
import com.walletconnect.sample.web3inbox.ui.routes.W3ISampleEvents
import com.walletconnect.sample.web3inbox.ui.routes.navigateToW3I
import timber.log.Timber

@Composable
fun AccountRoute(navController: NavController) {
    val viewModel: SelectAccountViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.walletEvents.collect { event ->
            Timber.d(event.toString())
            when (event) {
                is W3ISampleEvents.SessionApproved -> navController.navigateToW3I(event.account)
                else -> Unit
            }
        }
    }

    // If logged out then open account view
    if (SharedPrefStorage.getShouldOpenWeb3InboxTab(context)) {
        // If logged in before then go straight to web3inbox
        val account = SharedPrefStorage.getLastLoggedInAccount(context)
        if (account != null) {
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(80.dp)
                        .height(80.dp),
                    strokeWidth = 8.dp
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(fontSize = 24.sp, text = "Hello again!")
                Text(fontSize = 40.sp, text = account.split(":").last().let { it.take(6) + "..." + it.takeLast(4) })
            }

            return LaunchedEffect(key1 = Unit, block = {
                navController.navigateToW3I(account)
            })
        } else {
            LaunchedEffect(key1 = Unit, block = {
                viewModel.disconnectOldSessions()
            })
        }
    }

    AccountScreen(navController)
}

@Composable
fun SignInWithWalletSection(onClick: () -> Unit) {
    Text(
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f), textAlign = TextAlign.Center,
        text = "Good for sync testing\nNote: Selecting \"Log In With Wallet\" option will require signing messages on your wallet"
    )
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        onClick = onClick
    ) {
        Text(text = "Log In With Wallet")
    }
}