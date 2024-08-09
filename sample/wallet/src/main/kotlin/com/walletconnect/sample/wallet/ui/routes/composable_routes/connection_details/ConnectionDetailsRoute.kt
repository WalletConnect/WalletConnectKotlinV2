@file:OptIn(ExperimentalPagerApi::class, ExperimentalPagerApi::class, ExperimentalPagerApi::class)

package com.walletconnect.sample.wallet.ui.routes.composable_routes.connection_details

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.sample.common.ui.themedColor
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.ui.common.Content
import com.walletconnect.sample.wallet.ui.common.InnerContent
import com.walletconnect.sample.wallet.ui.common.blue.BlueLabelTexts
import com.walletconnect.sample.wallet.ui.common.getAllEventsByChainId
import com.walletconnect.sample.wallet.ui.common.getAllMethodsByChainId
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionType
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionUI
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsViewModel
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ConnectionDetailsRoute(navController: NavController, connectionId: Int?, connectionsViewModel: ConnectionsViewModel) {
    connectionsViewModel.currentConnectionId = connectionId
    val connectionUI by remember { connectionsViewModel.currentConnectionUI }
    var isEmitLoading by remember { mutableStateOf(false) }
    var isDeleteLoading by remember { mutableStateOf(false) }
    var isUpdateLoading by remember { mutableStateOf(false) }
    val composableScope = rememberCoroutineScope()
    val context = LocalContext.current

    connectionUI?.let { uiConnection ->
        Column(modifier = Modifier.fillMaxWidth()) {
            TopButtons(navController, isEmitAndUpdateVisible = uiConnection.type is ConnectionType.Sign, isEmitLoading = isEmitLoading, isUpdateLoading = isUpdateLoading,
                onEmit = {
                    when (uiConnection.type) {
                        is ConnectionType.Sign -> {
                            isEmitLoading = true

                            try {
                                val account = uiConnection.type.namespaces.values.first().accounts.first()
                                val lastDelimiterIndex = account.indexOfLast { it == ':' }
                                val chainId = account.dropLast(account.lastIndex - lastDelimiterIndex + 1)
                                val event = getAllEventsByChainId(uiConnection.type.namespaces.values.first(), account).first()
                                Web3Wallet.emitSessionEvent(
                                    Wallet.Params.SessionEmit(uiConnection.type.topic, Wallet.Model.SessionEvent(event, "someData"), chainId),
                                    onSuccess = {
                                        isEmitLoading = false
                                        composableScope.launch(Dispatchers.Main) {
                                            Toast.makeText(context, "Event emitted: ${it.event.name}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onError = { error ->
                                        isEmitLoading = false
                                        Firebase.crashlytics.recordException(error.throwable)
                                        composableScope.launch(Dispatchers.Main) {
                                            Toast.makeText(context, "Event emit error. Error: ${error.throwable.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            } catch (e: Exception) {
                                isEmitLoading = false
                                Firebase.crashlytics.recordException(e)
                                composableScope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, "Event emit error. Error:  ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                onUpdate = {
                    when (uiConnection.type) {
                        is ConnectionType.Sign -> {
                            isUpdateLoading = true
                            try {
                                val entry = uiConnection.type.namespaces.entries.find { entry -> entry.key == "eip155" } ?: throw Exception("Cannot find eip155 namespace")
                                val newNamespaces: Map<String, Wallet.Model.Namespace.Session> =
                                    mapOf(
                                        "eip155" to entry.value.copy(
                                            accounts = entry.value.accounts.plus("${entry.value.chains?.first()}:0xd10b8732e28f1be12c31eb1a45dca63b80ed3d6e"),
                                            chains = entry.value.chains,
                                            methods = entry.value.methods,
                                            events = entry.value.events,
                                        )
                                    ).toMutableMap()
                                val params = Wallet.Params.SessionUpdate(uiConnection.type.topic, newNamespaces)
                                Web3Wallet.updateSession(params,
                                    onSuccess = {
                                        isUpdateLoading = false
                                        connectionsViewModel.refreshConnections()
                                        composableScope.launch(Dispatchers.Main) {
                                            Toast.makeText(context, "Session updated", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onError = { error ->
                                        isUpdateLoading = false
                                        Firebase.crashlytics.recordException(error.throwable)
                                        composableScope.launch(Dispatchers.Main) {
                                            Toast.makeText(context, "Session update error. Error: ${error.throwable.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            } catch (e: Exception) {
                                isUpdateLoading = false
                                Firebase.crashlytics.recordException(e)
                                composableScope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, "Session update error. Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Connection(uiConnection)
            Spacer(modifier = Modifier.height(16.dp))
            ConnectionType(uiConnection, isDeleteLoading, connectionsViewModel,
                onDelete = {
                    when (uiConnection.type) {
                        is ConnectionType.Sign -> {
                            try {
                                isDeleteLoading = true
                                Web3Wallet.disconnectSession(Wallet.Params.SessionDisconnect(uiConnection.type.topic),
                                    onSuccess = {
                                        isDeleteLoading = false
                                        connectionsViewModel.refreshConnections()
                                        composableScope.launch(Dispatchers.Main) {
                                            navController.popBackStack()
                                            Toast.makeText(context, "Session disconnected", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onError = { error ->
                                        Firebase.crashlytics.recordException(error.throwable)
                                        isDeleteLoading = false
                                        connectionsViewModel.refreshConnections()
                                        composableScope.launch(Dispatchers.Main) {
                                            Toast.makeText(context, "Session disconnection error: ${error.throwable.message ?: "Unknown error please contact support"}", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                            } catch (e: Exception) {
                                Firebase.crashlytics.recordException(e)
                                isDeleteLoading = false
                                connectionsViewModel.refreshConnections()
                                composableScope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, "Session disconnection error: ${e.message ?: "Unknown error please contact support"}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                onSwitch = {
                    when (uiConnection.type) {
                        is ConnectionType.Sign -> {
                            val (namespace, reference, _) = connectionsViewModel.displayedAccounts[1].split(":")
                            val chainId = "$namespace:$reference"
                            val accountsToChange = connectionsViewModel.getAccountsToChange()
                            Web3Wallet.emitSessionEvent(Wallet.Params.SessionEmit(uiConnection.type.topic, Wallet.Model.SessionEvent("accountsChanged", accountsToChange), chainId),
                                onSuccess = {
                                    composableScope.launch(Dispatchers.Main) {
                                        Toast.makeText(context, "Switching account", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onError = { error ->
                                    Firebase.crashlytics.recordException(error.throwable)
                                    composableScope.launch(Dispatchers.Main) {
                                        Toast.makeText(context, "Switch account error: ${error.throwable.message ?: "Unknown error please contact support"}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                })
        }
    } ?: run {
        Text("Something went wrong :C")
    }
}

@Composable
fun ConnectionType(connectionUI: ConnectionUI, isLoading: Boolean, connectionsViewModel: ConnectionsViewModel, onDelete: () -> Unit, onSwitch: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        when (val type = connectionUI.type) {
            is ConnectionType.Sign -> Namespace(type.namespaces, connectionsViewModel)
        }

        AnimatedContent(targetState = isLoading, label = "Loading") { state ->
            if (state) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(8.dp)
                        .wrapContentWidth(align = Alignment.CenterHorizontally)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    color = themedColor(darkColor = 0xfff25a67, lightColor = 0xfff05142), strokeWidth = 4.dp
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .clickable { onSwitch() }
                        .padding(vertical = 5.dp),
                        text = "Switch Account",
                        style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, color = themedColor(darkColor = 0xFFe3e7e7, lightColor = 0xFF141414)))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .clickable { onDelete() }
                        .padding(vertical = 5.dp),
                        text = "Delete",
                        style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = themedColor(darkColor = 0xfff25a67, lightColor = 0xfff05142)))
                }
            }
        }
    }
}

@Composable
fun Namespace(namespaces: Map<String, Wallet.Model.Namespace.Session>, connectionsViewModel: ConnectionsViewModel) {
    val pagerState = rememberPagerState()
    val accounts = namespaces.flatMap { (namespace, session) -> session.accounts }.distinctBy { "${it.split(":")[0]}:${it.split(":")[1]}" }
    val accountsToSessions: Map<String, Wallet.Model.Namespace.Session> = namespaces.flatMap { (namespace, proposal) -> proposal.accounts.map { chain -> chain to proposal } }.toMap()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            modifier = Modifier.height(450.dp),
            count = accounts.size,
            state = pagerState,
        ) { current ->
            accounts[current].also { account -> ChainPermissions(account, accountsToSessions, connectionsViewModel) }
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
fun ChainPermissions(account: String, accountsToSessions: Map<String, Wallet.Model.Namespace.Session>, connectionsViewModel: ConnectionsViewModel) {
    val session: Wallet.Model.Namespace.Session = accountsToSessions[account]!!
    val lastDelimiterIndex = account.indexOfLast { it == ':' }
    val chainId = account.dropLast(account.lastIndex - lastDelimiterIndex + 1)
    Content(title = chainId.uppercase()) {

        val accountsToShow = session.accounts.filter { "${it.split(":")[0]}:${it.split(":")[1]}" == chainId }

        connectionsViewModel.displayedAccounts = accountsToShow
        Accounts(accountsToShow)
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
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(connectionUI.icon)
                    .size(60)
                    .crossfade(true)
                    .error(com.walletconnect.sample.common.R.drawable.ic_walletconnect_circle_blue)
                    .listener(
                        onSuccess = { request, metadata -> println("kobe: onSuccess: $request, $metadata") },
                        onError = { _, throwable -> println("Error loading image: ${throwable.throwable.message}") })
                    .build()
            )

            Image(
                painter = painter,
                contentDescription = "Connection image",
                modifier = iconModifier,
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center
            )
        } else {
            Icon(modifier = iconModifier.alpha(.7f), imageVector = ImageVector.vectorResource(id = R.drawable.sad_face), contentDescription = "Sad face")
        }
        Text(text = connectionUI.name, style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = themedColor(darkColor = 0xFFe3e7e7, lightColor = 0xFF141414)))
        Text(text = connectionUI.uri, style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = themedColor(darkColor = 0xFF788686, lightColor = 0xFF788686)))
    }
}

@Composable
fun TopButtons(navController: NavController, isEmitAndUpdateVisible: Boolean, isEmitLoading: Boolean, isUpdateLoading: Boolean, onEmit: () -> Unit, onUpdate: () -> Unit) {
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
        if (isEmitAndUpdateVisible) {
            AnimatedContent(targetState = isEmitLoading, label = "Loading") { state ->
                if (state) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                            .wrapContentWidth(align = Alignment.CenterHorizontally)
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        color = color, strokeWidth = 4.dp
                    )
                } else {
                    Text(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .clickable { onEmit() }
                            .padding(horizontal = 5.dp, vertical = 5.dp),
                        text = "Emit", style = style
                    )
                }
            }
            AnimatedContent(targetState = isUpdateLoading, label = "Loading") { state ->
                if (state) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                            .wrapContentWidth(align = Alignment.CenterHorizontally)
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        color = color, strokeWidth = 4.dp
                    )
                } else {
                    Text(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .clickable { onUpdate() }
                            .padding(horizontal = 5.dp, vertical = 5.dp),
                        text = "Update", style = style
                    )
                }
            }
        }
    }
}