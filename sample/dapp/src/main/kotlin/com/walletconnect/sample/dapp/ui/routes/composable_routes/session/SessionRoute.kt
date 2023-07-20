@file:OptIn(ExperimentalFoundationApi::class)

package com.walletconnect.sample.dapp.ui.routes.composable_routes.session

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.walletconnect.sample.common.ui.WCTopAppBar
import com.walletconnect.sample.common.ui.commons.BlueButton
import com.walletconnect.sample.common.ui.themedColor
import com.walletconnect.sample.dapp.R
import com.walletconnect.sample.dapp.ui.DappSampleEvents
import com.walletconnect.sample.dapp.ui.navigateToAccount
import com.walletconnect.sample.dapp.ui.routes.Route

@Composable
fun SessionRoute(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: SessionViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sessionEvent.collect { event ->
            when (event) {
                is DappSampleEvents.PingSuccess -> Toast.makeText(
                    context,
                    "Pinged Peer Successfully on Topic: ${event.topic}",
                    Toast.LENGTH_SHORT
                ).show()
                is DappSampleEvents.PingError -> Toast.makeText(
                    context,
                    "Pinged Peer Unsuccessfully",
                    Toast.LENGTH_SHORT
                ).show()
                is DappSampleEvents.Disconnect -> navController.popBackStack(
                    Route.ChainSelection.path,
                    inclusive = false
                )
                else -> Unit
            }
        }
    }

    SessionScreen(
        uiState = state,
        onBackPressed = navController::popBackStack,
        onSessionClick = navController::navigateToAccount,
        onPingClick = viewModel::ping,
        onDisconnectClick = viewModel::disconnect,
    )
}

@Composable
private fun SessionScreen(
    uiState: List<SessionUi>,
    onBackPressed: () -> Unit,
    onSessionClick: (String) -> Unit,
    onPingClick: () -> Unit,
    onDisconnectClick: () -> Unit,
) {
    Column {
        WCTopAppBar(
            titleText = "Session Chains",
            onBackIconClick = onBackPressed,
        )
        ChainsAction(onPingClick, onDisconnectClick)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    text = "Chains:",
                    modifier = Modifier.padding(8.dp),
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = themedColor(darkColor = 0xFFE5E7E7, lightColor = 0xFF141414)
                    )
                )
            }
            itemsIndexed(uiState) { _, item ->
                SessionChainItem(item, onSessionClick)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ChainsAction(
    onPingClick: () -> Unit,
    onDisconnectClick: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier
            .padding(8.dp)
            .animateContentSize()
            .clickable { isExpanded = !isExpanded }
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Actions")
                Image(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF3496ff))
                )
            }
            if (isExpanded) {
                SessionActions(
                    onPingClick = onPingClick,
                    onDisconnectClick = onDisconnectClick
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Gray)
                .height(1.dp)
        )
    }
}

@Composable
private fun SessionActions(
    onPingClick: () -> Unit,
    onDisconnectClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        val modifier = Modifier.padding(8.dp)
        BlueButton(text = "Ping", onClick = onPingClick, modifier = modifier)
        Spacer(modifier = Modifier.width(4.dp))
        BlueButton(text = "Disconnect", onClick = onDisconnectClick, modifier = modifier)
    }
}

@Composable
private fun LazyItemScope.SessionChainItem(
    item: SessionUi,
    onSessionClick: (String) -> Unit
) {
    Box(
        modifier = Modifier.animateItemPlacement(),
    ) {
        Column(
            modifier = Modifier
                .clickable { onSessionClick("${item.chainNamespace}:${item.chainReference}:${item.address}") }
                .fillMaxWidth()
                .padding(horizontal = 24.dp, 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = item.icon, contentDescription = null, Modifier.size(48.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.name,
                    style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.address,
                style = TextStyle(fontSize = 12.sp),
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 6.dp),
                overflow = TextOverflow.Ellipsis,
            )
        }
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_forward_chevron),
            contentDescription = "ForwardIcon",
            modifier = Modifier
                .padding(end = 8.dp)
                .size(12.dp)
                .align(Alignment.CenterEnd),
            colorFilter = ColorFilter.tint(Color(0xFF3496ff))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Gray)
                .height(1.dp)
        )
    }
}
