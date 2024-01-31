@file:OptIn(ExperimentalAnimationApi::class)

package com.walletconnect.wcmodal.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.walletconnect.modal.ui.components.common.VerticalSpacer
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.domain.WalletConnectModalDelegate
import com.walletconnect.wcmodal.ui.components.ModalRoot
import com.walletconnect.wcmodal.ui.components.RoundedMainButton
import com.walletconnect.wcmodal.ui.navigation.ModalNavGraph
import com.walletconnect.wcmodal.ui.theme.ModalTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
internal fun WalletConnectModal(
    navController: NavController
) {
    WalletConnectModalComponent(
        closeModal = { navController.popBackStack() }
    )
}

@ExperimentalAnimationApi
@Composable
internal fun WalletConnectModalComponent(closeModal: () -> Unit) {
    val context = LocalContext.current
    val viewModel: WalletConnectModalViewModel = viewModel()
    val navController = rememberAnimatedNavController()
    LaunchedEffect(Unit) {
        WalletConnectModalDelegate
            .wcEventModels
            .onEach { event -> event?.handleEvent(context, closeModal) }
            .collect()
    }

    ModalRoot(
        navController = navController,
        closeModal = closeModal,
    ) {
        ModalNavGraph(
            navController = navController,
            viewModel = viewModel
        )
    }
}


@Composable
internal fun LoadingModalState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        CircularProgressIndicator(color = ModalTheme.colors.main, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
internal fun ErrorModalState(retry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Something went wrong", style = TextStyle(color = ModalTheme.colors.onBackgroundColor, fontSize = 18.sp))
        VerticalSpacer(height = 10.dp)
        RoundedMainButton(
            text = "Retry",
            onClick = { retry() },
        )
    }
}

private fun Modal.Model.handleEvent(context: Context, closeModal: () -> Unit) {
    when (this) {
        is Modal.Model.ApprovedSession -> {
            closeModal()
        }
        is Modal.Model.Error -> {
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
        else -> Unit
    }
}
