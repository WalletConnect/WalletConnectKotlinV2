package com.walletconnect.sample.web3inbox.ui.routes.home.web3inbox

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.sample.web3inbox.domain.EthAccount
import com.walletconnect.web3.inbox.client.Web3Inbox
import timber.log.Timber


@Composable
fun Web3InboxRoute(navController: NavController) {

    val viewModel: Web3InboxViewModel = viewModel()
    val web3InboxState = Web3Inbox.rememberWeb3InboxState()

    val context = LocalContext.current

    Web3Inbox.View(state = web3InboxState)

    LaunchedEffect(context) {
        viewModel.random = EthAccount.Random(context)
        viewModel.requestStatus.collect { event ->
            Timber.d(event.toString())
            when (event) {
                is Web3InboxViewModel.OnSignRequestStatus.Success -> {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, event.session.redirect?.toUri()))
                    } catch (exception: ActivityNotFoundException) {
                        // There is no app to handle deep link
                    }
                }

                else -> {}
            }
        }
    }

}