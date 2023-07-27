package com.walletconnect.sample.web3inbox.ui.routes.home.web3inbox

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.walletconnect.sample.web3inbox.domain.EthAccount
import com.walletconnect.sample.web3inbox.domain.SharedPrefStorage
import com.walletconnect.sample.web3inbox.domain.Web3InboxInitializer
import com.walletconnect.web3.inbox.client.Web3Inbox
import timber.log.Timber


@Composable
fun Web3InboxRoute(navController: NavController, account: String) {
    val context = LocalContext.current
    Web3InboxInitializer.init(account, EthAccount.Random(context))
    val web3InboxState = Web3Inbox.rememberWeb3InboxState()

    Web3Inbox.View(state = web3InboxState)

    LaunchedEffect(context) {
        SharedPrefStorage.setShouldOpenWeb3InboxTab(context, true)
        Web3InboxInitializer.requestStatus.collect { event ->
            Timber.d(event.toString())
            when (event) {
                is Web3InboxInitializer.OnSignRequestStatus.Success -> {
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