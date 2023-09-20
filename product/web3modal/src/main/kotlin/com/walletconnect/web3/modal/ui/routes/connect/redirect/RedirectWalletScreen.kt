package com.walletconnect.web3.modal.ui.routes.connect.redirect

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.modal.utils.goToNativeWallet
import com.walletconnect.modal.utils.openPlayStore
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.ui.components.internal.commons.DeclinedIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.entry.CopyActionEntry
import com.walletconnect.web3.modal.ui.components.internal.commons.FullWidthDivider
import com.walletconnect.web3.modal.ui.components.internal.commons.LoadingBorder
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletImage
import com.walletconnect.web3.modal.ui.components.internal.commons.button.TryAgainButton
import com.walletconnect.web3.modal.ui.components.internal.commons.entry.StoreEntry
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.previews.testWallets
import com.walletconnect.web3.modal.ui.routes.connect.ConnectState
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun RedirectWalletRoute(
    connectState: ConnectState,
    wallet: Wallet
) {
    val uriHandler = LocalUriHandler.current
    val context: Context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var redirectState by remember { mutableStateOf<RedirectState>(RedirectState.Loading) }

    LaunchedEffect(Unit) {
        Web3ModalDelegate.wcEventModels.collect {
            redirectState = when (it) {
                is Modal.Model.RejectedSession -> RedirectState.Reject
                else -> RedirectState.Loading
            }
        }
    }

    RedirectWalletScreen(
        wallet = wallet,
        state = redirectState,
        onCopyLinkClick = {
            Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
            clipboardManager.setText(AnnotatedString(connectState.uri))
        },
        onRetry = {
            connectState.connect {
                redirectState = RedirectState.Loading
                uriHandler.goToNativeWallet(it, wallet.nativeLink)
            }
        },
        onOpenPlayStore = { uriHandler.openPlayStore(wallet.playStoreLink) })

    LaunchedEffect(Unit) {
        connectState.connect { uri ->
            wallet.nativeLink?.let {
                uriHandler.goToNativeWallet(uri, wallet.nativeLink)
            }
        }
    }
}

@Composable
private fun RedirectWalletScreen(
    wallet: Wallet,
    state: RedirectState,
    onCopyLinkClick: () -> Unit,
    onRetry: () -> Unit,
    onOpenPlayStore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VerticalSpacer(height = 28.dp)
        RedirectWalletState(wallet, state)
        VerticalSpacer(height = 20.dp)
        TryAgainButton(onClick = onRetry)
        VerticalSpacer(height = 20.dp)
        CopyActionEntry(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCopyLinkClick
        )
        VerticalSpacer(height = 16.dp)
        FullWidthDivider()
        VerticalSpacer(height = 16.dp)
        StoreEntry(text = "Get ${wallet.name}", onClick = onOpenPlayStore)
        VerticalSpacer(height = 16.dp)
    }
}

@Composable
private fun RedirectWalletState(
    wallet: Wallet, state: RedirectState
) {
    when (state) {
        RedirectState.Loading -> {
            WalletImageWithLoader(wallet.imageUrl)
            VerticalSpacer(height = 20.dp)
            Text(text = "Continue in ${wallet.name}", style = Web3ModalTheme.typo.paragraph500)
            VerticalSpacer(height = 8.dp)
            Text(
                text = "Accept connection request in your wallet app", style = Web3ModalTheme.typo.small500.copy(color = Web3ModalTheme.colors.foreground.color200)
            )
        }

        RedirectState.Reject -> {
            RejectedState(wallet.imageUrl)
        }
    }
}

@Composable
private fun WalletImageWithLoader(url: String) {
    LoadingBorder(
        cornerRadius = 25.dp
    ) {
        WalletImage(
            url = url, modifier = Modifier
                .size(80.dp)
                .border(width = 1.dp, color = Web3ModalTheme.colors.overlay10, shape = RoundedCornerShape(25.dp))
        )
    }
}

@Composable
private fun RejectedState(url: String) {
    Box {
        WalletImage(
            url = url, modifier = Modifier
                .size(80.dp)
                .border(width = 1.dp, color = Web3ModalTheme.colors.overlay10, shape = RoundedCornerShape(25.dp))
                .clip(RoundedCornerShape(25.dp))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Web3ModalTheme.colors.background.color100, shape = CircleShape)
                .padding(2.dp)
        ) {
            DeclinedIcon()
        }
    }
    VerticalSpacer(height = 20.dp)
    Text(text = "Connection declined", style = Web3ModalTheme.typo.paragraph500.copy(Web3ModalTheme.colors.error))
    VerticalSpacer(height = 8.dp)
    Text(
        text = "Connection can be declined if a previous request is still active",
        style = Web3ModalTheme.typo.small500.copy(color = Web3ModalTheme.colors.foreground.color200),
        modifier = Modifier.padding(horizontal = 30.dp),
        textAlign = TextAlign.Center
    )
}

@UiModePreview
@Composable
private fun PreviewRedirectWalletScreenWithLoadingState() {
    Web3ModalPreview("Metamask") {
        RedirectWalletScreen(wallet = testWallets.first(), RedirectState.Loading, {}, {}, {})
    }
}

@UiModePreview
@Composable
private fun PreviewRedirectWalletScreenWithRejectedState() {
    Web3ModalPreview("Metamask") {
        RedirectWalletScreen(wallet = testWallets.first(), RedirectState.Reject, {}, {}, {})
    }
}
