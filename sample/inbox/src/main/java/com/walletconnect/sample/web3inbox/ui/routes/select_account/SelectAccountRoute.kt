package com.walletconnect.sample.web3inbox.ui.routes.select_account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.sample.web3inbox.domain.EthAccount
import com.walletconnect.sample.web3inbox.domain.SharedPrefStorage
import com.walletconnect.sample.web3inbox.ui.routes.W3ISampleEvents
import com.walletconnect.sample.web3inbox.ui.routes.navigateToW3I
import com.walletconnect.wcmodal.ui.openWalletConnectModal
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

    // If logged in before and is remember then go straight to login
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

    AccountScreen(navController, viewModel)
}

@Composable
fun AccountScreen(navController: NavController, viewModel: SelectAccountViewModel) {
    val context = LocalContext.current
    val random = EthAccount.Random(context)

    Box() {
        Column(
            verticalArrangement = Arrangement.Center, modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            RandomAccountSection(random) {
                SharedPrefStorage.saveLastLoggedInAccount(context, random.caip10())
                navController.navigateToW3I(random.caip10())
            }
            HorizontalLineDivider()
            SignInWithWalletSection(onClick = { viewModel.connectToWallet { uri -> navController.openWalletConnectModal(uri) } })
            HorizontalLineDivider()
            BurnerAccountSection() { navController.navigateToW3I(EthAccount.Burner.caip10()) }
            HorizontalLineDivider()
            FixedAccountSection {
                SharedPrefStorage.saveLastLoggedInAccount(context, random.caip10())
                navController.navigateToW3I(EthAccount.Fixed.caip10())
            }
        }
    }
}

@Composable
fun RandomAccountSection(random: EthAccount.Random, onClick: () -> Unit) {
    Text(
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f),
        textAlign = TextAlign.Center,
        text = "This is the way W3I SDK works\nNote: Generates and saves random account for this app"
    )
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Random account")
            Text(fontSize = 8.sp, text = random.address)
        }
    }
}

@Composable
fun SignInWithWalletSection(onClick: () -> Unit) {
    Text(
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f), textAlign = TextAlign.Center,
        text = "Wanna use your wallet?\nGood for sync testing\nNote: Selecting \"Log In With Wallet\" option will require signing messages on your wallet"
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

@Composable
fun BurnerAccountSection(onClick: () -> Unit) {
    Text(
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f),
        textAlign = TextAlign.Center,
        text = "For Advanced testers.\nNote: This might have issues with persisting data. Please use Random one"
    )
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Burner account")
            Text(fontSize = 8.sp, text = EthAccount.Burner.address)
        }
    }
}

@Composable
fun FixedAccountSection(onClick: () -> Unit) {
    Text(
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f),
        textAlign = TextAlign.Center,
        text = "For Advanced testers.\nNote: This might have issues if too many ppl use it. Please use Random one"
    )
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Fixed account")
            Text(fontSize = 8.sp, text = EthAccount.Fixed.address)
        }
    }
}

@Composable
fun HorizontalLineDivider() {
    Spacer(modifier = Modifier.height(20.dp))
    Spacer(
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
            .alpha(0.5f)
            .background(Color.Black)
    )
    Spacer(modifier = Modifier.height(20.dp))
}
