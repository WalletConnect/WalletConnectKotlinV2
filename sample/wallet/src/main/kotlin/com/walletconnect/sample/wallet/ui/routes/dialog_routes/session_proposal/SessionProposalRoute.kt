package com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_proposal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.walletconnect.sample.wallet.ui.common.Buttons
import com.walletconnect.sample.wallet.ui.common.Content
import com.walletconnect.sample.wallet.ui.common.SemiTransparentDialog
import com.walletconnect.sample.wallet.ui.common.blue.BlueLabelTexts
import com.walletconnect.sample.wallet.ui.common.getAllEventsByChainId
import com.walletconnect.sample.wallet.ui.common.getAllMethodsByChainId
import com.walletconnect.sample.wallet.ui.common.peer.Peer
import com.walletconnect.sample.wallet.ui.routes.Route
import com.walletconnect.sample.wallet.ui.routes.showSnackbar
import com.walletconnect.sample.common.CompletePreviews
import com.walletconnect.sample.common.sendResponseDeepLink
import com.walletconnect.sample.common.ui.theme.PreviewTheme
import com.walletconnect.sample.common.ui.themedColor
import com.walletconnect.web3.wallet.client.Wallet
import kotlinx.coroutines.launch

@CompletePreviews
@Composable
fun SessionProposalRoutePreview() {
    PreviewTheme {
        SessionProposalRoute(rememberNavController())
    }
}

@Composable
fun SessionProposalRoute(navController: NavHostController, sessionProposalViewModel: SessionProposalViewModel = viewModel()) {
    val sessionProposalUI = sessionProposalViewModel.sessionProposal ?: throw Exception("Missing session proposal")
    val composableScope = rememberCoroutineScope()
    val context = LocalContext.current
    SemiTransparentDialog {
        Spacer(modifier = Modifier.height(24.dp))
        Peer(peerUI = sessionProposalUI.peerUI, "would like to connect", sessionProposalUI.peerContext)
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        Permissions(sessionProposalUI = sessionProposalUI)
        Spacer(modifier = Modifier.height(16.dp))
        Buttons(onDecline = {
            composableScope.launch {
                try {
                    sessionProposalViewModel.reject(sessionProposalUI.pubKey) { redirect ->
                        if (redirect.isNotEmpty()){
                            context.sendResponseDeepLink(redirect.toUri())
                        }
                    }
                    navController.popBackStack(route = Route.Connections.path, inclusive = false)
                } catch (e: Throwable) {
                    closeAndShowError(navController, e.message)
                }
            }
        }, onAllow = {
            composableScope.launch {
                try {
                    sessionProposalViewModel.approve(sessionProposalUI.pubKey) { redirect ->
                        if (redirect.isNotEmpty()) {
                            context.sendResponseDeepLink(redirect.toUri())
                        }
                    }
                    navController.popBackStack(route = Route.Connections.path, inclusive = false)
                } catch (e: Throwable) {
                    closeAndShowError(navController, e.message)
                }
            }
        })
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun closeAndShowError(navController: NavHostController, mesage: String?) {
    navController.popBackStack(route = Route.Connections.path, inclusive = false)
    navController.showSnackbar(mesage ?: "Session proposal error, please check your Internet connection")
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
    val chains = sessionProposalUI.namespaces.flatMap { (namespaceKey, proposal) ->
        if (proposal.chains != null) {
            proposal.chains!!
        } else {
            listOf(namespaceKey)
        }
    }

    val chainsToProposals: Map<String, Wallet.Model.Namespace.Proposal> =
        sessionProposalUI.namespaces.flatMap { (namespaceKey, proposal) ->
            if (proposal.chains != null) {
                proposal.chains!!.map { chain -> chain to proposal }
            } else {
                listOf(namespaceKey).map { chain -> chain to proposal }
            }
        }.toMap()

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
