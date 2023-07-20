package com.walletconnect.sample.dapp.ui.routes.composable_routes.account

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.walletconnect.sample.dapp.ui.DappSampleEvents
import com.walletconnect.sample.dapp.ui.openMessageDialog
import com.walletconnect.sample.dapp.ui.routes.Route
import com.walletconnect.sample.common.ui.commons.BlueButton
import com.walletconnect.sample.common.ui.commons.FullScreenLoader
import timber.log.Timber

@Composable
fun AccountRoute(navController: NavController) {
    val viewModel: AccountViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAccountDetails()

        viewModel.events.collect { event ->
            when (event) {
                is DappSampleEvents.RequestSuccess -> { navController.openMessageDialog(event.result) }
                is DappSampleEvents.RequestPeerError -> { navController.openMessageDialog(event.errorMsg) }
                is DappSampleEvents.RequestError -> { navController.openMessageDialog(event.exceptionMsg) }
                is DappSampleEvents.Disconnect -> navController.popBackStack(Route.ChainSelection.path, false)
                else -> Unit
            }
        }
    }

    AccountScreen(
        state = state,
        onMethodClick = viewModel::requestMethod
    )
}

@Composable
private fun AccountScreen(
    state: AccountUi,
    onMethodClick: (String, (Uri) -> Unit) -> Unit,
) {
    when (state) {
        AccountUi.Loading -> FullScreenLoader()
        is AccountUi.AccountData -> AccountContent(state, onMethodClick)
    }
}

@Composable
fun AccountContent(
    state: AccountUi.AccountData,
    onMethodClick: (String, (Uri) -> Unit) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        ChainData(chain = state)
        Spacer(modifier = Modifier.height(6.dp))
        MethodList(
            methods = state.listOfMethods,
            onMethodClick = onMethodClick
        )
    }
}

@Composable
private fun ChainData(chain: AccountUi.AccountData) {
    Column(
        modifier = Modifier
            .clickable { }
            .fillMaxWidth()
            .padding(horizontal = 24.dp, 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = chain.icon, contentDescription = null, Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = chain.chainName,
                style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = chain.account,
            style = TextStyle(fontSize = 12.sp),
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 6.dp),
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MethodList(
    methods: List<String>,
    onMethodClick: (String, (Uri) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(methods) { _, item ->
            BlueButton(
                text = item,
                onClick = {
                    onMethodClick(item) { uri ->
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (e: Exception) {
                            Timber.tag("AccountRoute").d("Activity not found: $e")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
