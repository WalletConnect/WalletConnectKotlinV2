@file:OptIn(ExperimentalPagerApi::class, ExperimentalPagerApi::class, ExperimentalPagerApi::class)

package com.walletconnect.sample.wallet.ui.routes.composable_routes.connection_details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.skydoves.landscapist.glide.GlideImage
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.domain.accounts
import com.walletconnect.sample.wallet.ui.common.Content
import com.walletconnect.sample.wallet.ui.common.InnerContent
import com.walletconnect.sample.wallet.ui.common.blue.BlueLabelTexts
import com.walletconnect.sample.wallet.ui.common.getAllEventsByChainId
import com.walletconnect.sample.wallet.ui.common.getAllMethodsByChainId
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionType
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionUI
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsViewModel
import com.walletconnect.sample.wallet.ui.routes.showSnackbar
import com.walletconnect.sample.common.ui.themedColor
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet

@Composable
fun ConnectionDetailsRoute(navController: NavController, connectionId: Int?, connectionsViewModel: ConnectionsViewModel) {
    connectionsViewModel.currentConnectionId = connectionId
    val connectionUI by remember { connectionsViewModel.currentConnectionUI }

    connectionUI?.let { connectionUI ->
        Column(modifier = Modifier.fillMaxWidth()) {
            TopButtons(navController, isEmitVisible = connectionUI.type is ConnectionType.Sign) {
                when (connectionUI.type) {
                    is ConnectionType.Sign -> {
                        val account = connectionUI.type.namespaces.values.first().accounts.first()
                        val lastDelimiterIndex = account.indexOfLast { it == ':' }
                        val chainId = account.dropLast(account.lastIndex - lastDelimiterIndex + 1)
                        val event = getAllEventsByChainId(connectionUI.type.namespaces.values.first(), account).first()

                        Web3Wallet.emitSessionEvent(Wallet.Params.SessionEmit(connectionUI.type.topic, event = Wallet.Model.SessionEvent(event, "someData"), chainId)) {
                            Firebase.crashlytics.recordException(it.throwable)
                            navController.showSnackbar("Event emit error. Check logs")
                        }
                        navController.showSnackbar("Event emitted")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Connection(connectionUI)
            Spacer(modifier = Modifier.height(16.dp))
            ConnectionType(connectionUI, onDelete = {
                when (connectionUI.type) {
                    is ConnectionType.Sign -> {
                        Web3Wallet.disconnectSession(Wallet.Params.SessionDisconnect(connectionUI.type.topic)) { error ->
                            Firebase.crashlytics.recordException(error.throwable)
                        }
                        connectionsViewModel.refreshConnections()
                        navController.popBackStack()
                    }
                }
            })
        }
    } ?: run {
        Text("Something went wrong :C")
    }
}

@Composable
fun ConnectionType(connectionUI: ConnectionUI, onDelete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        when (val type = connectionUI.type) {
            is ConnectionType.Sign -> Namespace(type.namespaces)
        }

        Text(modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .clickable { onDelete() }
            .padding(horizontal = 20.dp, vertical = 5.dp),
            text = "Delete",
            style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = themedColor(darkColor = 0xfff25a67, lightColor = 0xfff05142)))
    }
}

@Composable
fun Namespace(namespaces: Map<String, Wallet.Model.Namespace.Session>) {
    val pagerState = rememberPagerState()
    val accounts = namespaces.flatMap { (namespace, session) -> session.accounts }
    val accountsToSessions: Map<String, Wallet.Model.Namespace.Session> = namespaces.flatMap { (namespace, proposal) -> proposal.accounts.map { chain -> chain to proposal } }.toMap()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            modifier = Modifier.height(450.dp),
            count = accounts.size,
            state = pagerState,
        ) { current ->
            accounts[current].also { chain -> ChainPermissions(chain, accountsToSessions) }
        }

        if (accounts.size > 1) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                inactiveColor = themedColor(darkColor = Color(0xFFE4E4E7).copy(alpha = .12f), lightColor = Color(0xFF505059).copy(.1f)),
                activeColor = themedColor(darkColor = Color(0xFFE4E4E7).copy(alpha = .2f), lightColor = Color(0xFF505059).copy(.2f)),
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ChainPermissions(account: String, accountsToSessions: Map<String, Wallet.Model.Namespace.Session>) {
    val session: Wallet.Model.Namespace.Session = accountsToSessions[account]!!
    val lastDelimiterIndex = account.indexOfLast { it == ':' }
    val chainId = account.dropLast(account.lastIndex - lastDelimiterIndex + 1)
    Content(title = chainId.uppercase()) {
        Accounts(accounts.filter { it.first.chainId == chainId }.map { (chain, address) -> "${chain.chainId}:$address" })

        val sections = mapOf("Methods" to getAllMethodsByChainId(session, account), "Events" to getAllEventsByChainId(session, account))
        sections.forEach { (title, values) -> BlueLabelTexts(title, values, title != "Events") }
    }
}

@Composable
fun Accounts(accounts: List<String>) {
    InnerContent {
        Row {
            Text(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 13.dp),
                text = "Accounts", style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = themedColor(darkColor = Color(0xFF9ea9a9), lightColor = Color(0xFF788686)))
            )
        }


        accounts.forEachIndexed { index, account ->
            Text(
                text = account,
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = themedColor(darkColor = Color(0xff1cc6ff), lightColor = Color(0xFF05ace5))
                ),
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(horizontal = 5.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color = themedColor(darkColor = Color(0xFF153B47), lightColor = Color(0xFFDDF1F8)))
                    .padding(start = 8.dp, top = 3.dp, end = 8.dp, bottom = 5.dp)
            )
            if (index != accounts.lastIndex) Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
    Spacer(modifier = Modifier.height(5.dp))
}

@Composable
fun Connection(connectionUI: ConnectionUI) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        val iconModifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .border(width = 1.dp, shape = CircleShape, color = themedColor(darkColor = Color(0xFF191919), lightColor = Color(0xFFE0E0E0)))
        if (connectionUI.icon?.isNotBlank() == true) {
            GlideImage(modifier = iconModifier, imageModel = { connectionUI.icon })
        } else {
            Icon(modifier = iconModifier.alpha(.7f), imageVector = ImageVector.vectorResource(id = R.drawable.sad_face), contentDescription = "Sad face")
        }
        Text(text = connectionUI.name, style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = themedColor(darkColor = 0xFFe3e7e7, lightColor = 0xFF141414)))
        Text(text = connectionUI.uri, style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = themedColor(darkColor = 0xFF788686, lightColor = 0xFF788686)))
    }
}

@Composable
fun TopButtons(navController: NavController, isEmitVisible: Boolean, onEmit: () -> Unit) {
    val color = Color(0xFF3496ff)
    val style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = color)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val interactionSourceRow = remember { MutableInteractionSource() }
        Row(modifier = Modifier
            .clickable(interactionSource = interactionSourceRow, indication = null) { navController.popBackStack() }
            .padding(5.dp)
        ) {
            Icon(tint = color, imageVector = ImageVector.vectorResource(id = com.walletconnect.sample.common.R.drawable.chevron_left), contentDescription = "Go back")
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "Connections", style = style)
        }
        if (isEmitVisible) {
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .clickable { onEmit() }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                text = "Emit", style = style
            )
        }
    }
}