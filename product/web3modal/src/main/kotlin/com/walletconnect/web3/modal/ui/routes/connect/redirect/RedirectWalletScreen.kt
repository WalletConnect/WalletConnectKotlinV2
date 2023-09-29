package com.walletconnect.web3.modal.ui.routes.connect.redirect

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
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
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.modal.utils.openWebAppLink
import com.walletconnect.modal.utils.openMobileLink
import com.walletconnect.modal.utils.openPlayStore
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.ui.components.internal.commons.DeclinedIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.ExternalIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.entry.CopyActionEntry
import com.walletconnect.web3.modal.ui.components.internal.commons.FullWidthDivider
import com.walletconnect.web3.modal.ui.components.internal.commons.LoadingBorder
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletImage
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ImageButton
import com.walletconnect.web3.modal.ui.components.internal.commons.button.TryAgainButton
import com.walletconnect.web3.modal.ui.components.internal.commons.entry.StoreEntry
import com.walletconnect.web3.modal.ui.components.internal.commons.switch.PlatformTab
import com.walletconnect.web3.modal.ui.components.internal.commons.switch.PlatformTabRow
import com.walletconnect.web3.modal.ui.components.internal.commons.switch.rememberWalletPlatformTabs
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
    var platformTab by rememberWalletPlatformTabs(wallet.toPlatform())

    LaunchedEffect(Unit) {
        Web3ModalDelegate.wcEventModels.collect {
            redirectState = when (it) {
                is Modal.Model.RejectedSession -> RedirectState.Reject
                else -> RedirectState.Loading
            }
        }
    }

    LaunchedEffect(Unit) {
        connectState.connect { uri ->
            uriHandler.openMobileLink(
                uri = uri,
                mobileLink = wallet.mobileLink,
                onError = { redirectState = RedirectState.NotDetected }
            )
        }
    }

    RedirectWalletScreen(
        redirectState = redirectState,
        platformTab = platformTab,
        onPlatformTabSelect = { platformTab = it },
        wallet = wallet,
        onCopyLinkClick = {
            Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
            clipboardManager.setText(AnnotatedString(connectState.uri))
        },
        onMobileRetry = {
            connectState.connect { uri ->
                redirectState = RedirectState.Loading
                uriHandler.openMobileLink(
                    uri = uri,
                    mobileLink = wallet.mobileLink,
                    onError = { redirectState = RedirectState.NotDetected }
                )
            }
        },
        onOpenPlayStore = { uriHandler.openPlayStore(wallet.playStore) },
        onOpenWebApp = {
            connectState.connect {
                uriHandler.openWebAppLink(it, wallet.webAppLink)
            }
        }
    )
}

@Composable
private fun RedirectWalletScreen(
    redirectState: RedirectState,
    platformTab: PlatformTab,
    onPlatformTabSelect: (PlatformTab) -> Unit,
    wallet: Wallet,
    onCopyLinkClick: () -> Unit,
    onMobileRetry: () -> Unit,
    onOpenPlayStore: () -> Unit,
    onOpenWebApp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (wallet.hasMobileWallet && wallet.hasWebApp) {
            PlatformTabRow(platformTab, onPlatformTabSelect)
        }
        VerticalSpacer(height = 28.dp)
        when {
            platformTab == PlatformTab.WEB || redirectState == RedirectState.Loading -> WalletImageWithLoader(url = wallet.imageUrl)
            redirectState == RedirectState.NotDetected || redirectState == RedirectState.Reject -> RejectWalletImage(wallet.imageUrl)
        }
        VerticalSpacer(height = 20.dp)
        PlatformBox(
            platformTab = platformTab,
            mobileWalletContent = {
                RedirectMobileWalletScreen(
                    wallet = wallet,
                    state = redirectState,
                    onCopyLinkClick = onCopyLinkClick,
                    onRetry = onMobileRetry,
                    onOpenPlayStore = onOpenPlayStore,
                )
            },
            webWalletContent = {
                RedirectWebWalletScreen(
                    onCopyLinkClick = onCopyLinkClick,
                    onOpenWebApp = onOpenWebApp
                )
            }
        )
    }
}

@Composable
private fun PlatformBox(
    platformTab: PlatformTab,
    mobileWalletContent: @Composable () -> Unit,
    webWalletContent: @Composable () -> Unit
) {

    AnimatedContent(targetState = platformTab, label = "Platform state") { state ->
        when (state) {
            PlatformTab.MOBILE -> mobileWalletContent()
            PlatformTab.WEB -> webWalletContent()
        }
    }
}

@Composable
private fun RedirectMobileWalletScreen(
    wallet: Wallet,
    state: RedirectState,
    onCopyLinkClick: () -> Unit,
    onRetry: () -> Unit,
    onOpenPlayStore: () -> Unit
) {
    AnimatedContent(
        targetState = state,
        label = "Redirect Connect Animation",
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (it) {
                RedirectState.Loading -> LoadingState(wallet, onRetry, onCopyLinkClick)
                RedirectState.Reject -> RejectedState(onRetry)
                RedirectState.NotDetected -> NotDetectedWalletState(wallet, onOpenPlayStore)
            }
        }
    }
}

@Composable
private fun RedirectWebWalletScreen(
    onCopyLinkClick: () -> Unit,
    onOpenWebApp: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Open and continue in a browser", style = Web3ModalTheme.typo.paragraph500)
        VerticalSpacer(height = 20.dp)
        ImageButton(
            text = "Open",
            image = { ExternalIcon(it) },
            style = ButtonStyle.ACCENT,
            size = ButtonSize.M,
            onClick = onOpenWebApp
        )
        VerticalSpacer(height = 20.dp)
        CopyActionEntry(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCopyLinkClick
        )
    }
}

private fun Wallet.toPlatform(): PlatformTab = when {
    hasMobileWallet -> PlatformTab.MOBILE
    hasWebApp -> PlatformTab.WEB
    else -> PlatformTab.MOBILE
}

@Composable
private fun LoadingState(
    wallet: Wallet,
    onRetry: () -> Unit,
    onCopyLinkClick: () -> Unit
) {
    Text(text = "Continue in ${wallet.name}", style = Web3ModalTheme.typo.paragraph500)
    VerticalSpacer(height = 8.dp)
    Text(text = "Accept connection request in your wallet app", style = Web3ModalTheme.typo.small500.copy(color = Web3ModalTheme.colors.foreground.color200))
    VerticalSpacer(height = 20.dp)
    TryAgainButton(onClick = onRetry)
    VerticalSpacer(height = 20.dp)
    CopyActionEntry(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCopyLinkClick
    )
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
private fun RejectedState(onRetry: () -> Unit) {
    Text(text = "Connection declined", style = Web3ModalTheme.typo.paragraph500.copy(Web3ModalTheme.colors.error))
    VerticalSpacer(height = 8.dp)
    Text(
        text = "Connection can be declined if a previous request is still active",
        style = Web3ModalTheme.typo.small500.copy(color = Web3ModalTheme.colors.foreground.color200),
        modifier = Modifier.padding(horizontal = 30.dp),
        textAlign = TextAlign.Center
    )
    VerticalSpacer(height = 20.dp)
    TryAgainButton(onClick = onRetry)
}

@Composable
private fun NotDetectedWalletState(
    wallet: Wallet,
    onOpenPlayStore: () -> Unit
) {
    Text(text = "Download ${wallet.name}", style = Web3ModalTheme.typo.paragraph500)
    VerticalSpacer(height = 8.dp)
    Text(text = "Install ${wallet.name} app to continue", style = Web3ModalTheme.typo.small500.copy(color = Web3ModalTheme.colors.foreground.color200))
    VerticalSpacer(height = 16.dp)
    FullWidthDivider()
    VerticalSpacer(height = 16.dp)
    StoreEntry(text = "Get ${wallet.name}", onClick = onOpenPlayStore)
}

@Composable
private fun RejectWalletImage(url: String) {
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
}

@UiModePreview
@Composable
private fun PreviewRedirectWalletScreenWithLoadingState() {
    val wallet = testWallets.first()
    Web3ModalPreview(wallet.name) {
        RedirectWalletScreen(redirectState = RedirectState.Loading, platformTab = PlatformTab.MOBILE, {}, wallet, {}, {}, {}, {})
    }
}

@UiModePreview
@Composable
private fun PreviewRedirectWalletScreenWithRejectedState() {
    val wallet = testWallets.first()
    Web3ModalPreview(wallet.name) {
        RedirectWalletScreen(redirectState = RedirectState.Reject, platformTab = PlatformTab.MOBILE, {}, wallet, {}, {}, {}, {})
    }
}

@UiModePreview
@Composable
private fun PreviewRedirectWalletScreenWithNotDetectedState() {
    val wallet = testWallets.first()
    Web3ModalPreview(wallet.name) {
        RedirectWalletScreen(redirectState = RedirectState.NotDetected, platformTab = PlatformTab.MOBILE, {}, wallet, {}, {}, {}, {})
    }
}

@UiModePreview
@Composable
private fun PreviewRedirectWebWalletScreen() {
    val wallet = testWallets.first()
    Web3ModalPreview(wallet.name) {
        RedirectWalletScreen(redirectState = RedirectState.Loading, platformTab = PlatformTab.WEB, {}, wallet, {}, {}, {}, {})
    }
}
