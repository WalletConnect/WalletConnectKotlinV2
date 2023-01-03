@file:OptIn(ExperimentalPagerApi::class, ExperimentalPagerApi::class)

package com.walletconnect.web3.wallet.ui.routes.dialog_routes.session_proposal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.walletconnect.web3.wallet.ui.common.*
import com.walletconnect.web3.wallet.ui.common.peer.Peer
import com.walletconnect.web3.wallet.ui.routes.Route
import com.walletconnect.web3.wallet.ui.theme.Web3WalletTheme
import com.walletconnect.web3.wallet.ui.utils.CompletePreviews
import com.walletconnect.web3.wallet.ui.common.themedColor
import com.walletconnect.web3.wallet.client.Wallet

@CompletePreviews
@Composable
fun SessionProposalRoutePreview() {
    Web3WalletTheme {
        SessionProposalRoute(rememberNavController())
    }
}

@Composable
fun SessionProposalRoute(navController: NavHostController, sessionProposalViewModel: SessionProposalViewModel = viewModel()) {
    val sessionProposalUI = sessionProposalViewModel.sessionProposal ?: throw Exception("Missing session proposal")
    SemiTransparentDialog {
        Spacer(modifier = Modifier.height(24.dp))
        Peer(peerUI = sessionProposalUI.peerUI, "would like to connect")
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        Permissions(sessionProposalUI = sessionProposalUI)
        Spacer(modifier = Modifier.height(16.dp))
        Buttons(onDecline = {
            sessionProposalViewModel.reject()
            navController.popBackStack(route =  Route.Connections.path, inclusive = false)
        }, onAllow = {
            sessionProposalViewModel.approve()
            navController.popBackStack(route =  Route.Connections.path, inclusive = false)
        })
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(themedColor(darkColor = Color(0xFFE4E4E7).copy(0.12f), lightColor = Color(0xFF3C3C43).copy(0.12f)))
    )
}

@Composable
fun Permissions(sessionProposalUI: SessionProposalUI) {
    val pagerState = rememberPagerState()
    val chains = sessionProposalUI.namespaces.flatMap { (namespace, proposal) -> proposal.chains }
    val chainsToProposals: Map<String, Wallet.Model.Namespace.Proposal> = sessionProposalUI.namespaces.flatMap { (namespace, proposal) -> proposal.chains.map { chain -> chain to proposal } }.toMap()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Requested permissions".uppercase(), style = TextStyle(
                fontWeight = FontWeight.Medium, fontSize = 12.sp, color = themedColor(Color(0xFFD7D7DB).copy(.5f), Color(0xF3C3C43).copy(.4f))
            )
        )

        HorizontalPager(
            modifier = Modifier.height(400.dp),
            count = chains.size,
            state = pagerState,
        ) { current ->
            chains[current].also { chain -> ChainPermissions(chain, chainsToProposals) }
        }

        if (chains.size > 1) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                inactiveColor = themedColor(darkColor = Color(0xFFE4E4E7).copy(alpha = .12f), lightColor = Color(0xFF505059).copy(.1f)),
                activeColor = themedColor(darkColor = Color(0xFFE4E4E7).copy(alpha = .2f), lightColor = Color(0xFF505059).copy(.2f)),
            )
        }
    }
}

@Composable
fun ChainPermissions(chain: String, chainsToProposals: Map<String, Wallet.Model.Namespace.Proposal>) {
    val proposal = chainsToProposals[chain]!!
    Content(title = chain.uppercase()) {
        val sections = mapOf("Methods" to getAllMethodsByChainId(proposal, chain), "Events" to getAllEventsByChainId(proposal, chain))
        sections.forEach { (title, values) -> BlueLabelTexts(title, values, title != "Events") }
    }
}
