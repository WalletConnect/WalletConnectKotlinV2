package com.walletconnect.sample.web3inbox.ui.routes.select_account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.sample.web3inbox.domain.EthAccount
import com.walletconnect.sample.web3inbox.domain.SharedPrefStorage
import com.walletconnect.sample.web3inbox.ui.routes.navigateToW3I
import com.walletconnect.wcmodal.ui.openWalletConnectModal

@Composable
fun AccountScreen(navController: NavController) {
    val context = LocalContext.current
    val random = EthAccount.Random(context)

    Box {
        Column(
            verticalArrangement = Arrangement.Center, modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            RandomAccountSection(random) {
                SharedPrefStorage.setLastLoggedInAccount(context, random.caip10())
                navController.navigateToW3I(random.caip10())
            }
            HorizontalLineDivider()
            SignInWithWalletSection(onClick = { navController.openWalletConnectModal() })
            HorizontalLineDivider()
            BurnerAccountSection {
                SharedPrefStorage.setLastLoggedInAccount(context, EthAccount.Burner.caip10())
                navController.navigateToW3I(EthAccount.Burner.caip10())
            }
            HorizontalLineDivider()
            FixedAccountSection {
                SharedPrefStorage.setLastLoggedInAccount(context, EthAccount.Fixed.caip10())
                navController.navigateToW3I(EthAccount.Fixed.caip10())
            }
        }
    }
}