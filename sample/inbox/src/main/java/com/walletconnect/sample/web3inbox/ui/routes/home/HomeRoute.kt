package com.walletconnect.sample.web3inbox.ui.routes.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.sample.web3inbox.R
import com.walletconnect.sample.web3inbox.ui.routes.home.settings.SettingsRoute
import com.walletconnect.sample.web3inbox.ui.routes.home.web3inbox.Web3InboxRoute
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeRoute(navController: NavController, account: String) {
    val pageCount = 2
    val pagerState = rememberPagerState() { pageCount }
    val coroutineScope = rememberCoroutineScope()

    val onBackPressed = {
        // Do nothing
    }

    BackHandler(enabled = true, onBackPressed)

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(modifier = Modifier.weight(1f), state = pagerState, beyondBoundsPageCount = 1) { page ->
            when (page) {
                0 -> Web3InboxRoute(navController = navController, account)
                1 -> SettingsRoute(navController = navController)
            }
        }
        BottomAppBar() {
            BottomNavigationItem(selected = pagerState.currentPage == 0, onClick = {
                coroutineScope.launch { pagerState.animateScrollToPage(0) }
            }, icon = {
                Icon(modifier = Modifier.width(28.dp), painter = painterResource(id = R.drawable.ic_wallet_connect_logo), contentDescription = "W3I")
            })
            BottomNavigationItem(selected = pagerState.currentPage == 1, onClick = {
                coroutineScope.launch { pagerState.animateScrollToPage(1) }
            }, icon = {
                Icon(modifier = Modifier.width(32.dp), painter = painterResource(id = R.drawable.ic_settings), contentDescription = "Settings")
            })
        }
    }
}
