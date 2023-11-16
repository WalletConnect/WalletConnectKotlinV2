package com.walletconnect.web3.modal.ui.routes.connect.redirect

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.modal.utils.openMobileLink
import com.walletconnect.modal.utils.openPlayStore
import com.walletconnect.modal.utils.openWebAppLink
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.ui.components.internal.OrientationBox
import com.walletconnect.web3.modal.ui.components.internal.commons.DeclinedIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.ExternalIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.LoadingBorder
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletImage
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ChipButton
import com.walletconnect.web3.modal.ui.components.internal.commons.button.TryAgainButton
import com.walletconnect.web3.modal.ui.components.internal.commons.entry.CopyActionEntry
import com.walletconnect.web3.modal.ui.components.internal.commons.entry.StoreEntry
import com.walletconnect.web3.modal.ui.components.internal.commons.switch.PlatformTab
import com.walletconnect.web3.modal.ui.components.internal.commons.switch.PlatformTabRow
import com.walletconnect.web3.modal.ui.components.internal.commons.switch.rememberWalletPlatformTabs
import com.walletconnect.web3.modal.ui.components.internal.snackbar.LocalSnackBarHandler
import com.walletconnect.web3.modal.ui.previews.Landscape
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
    val snackBar = LocalSnackBarHandler.current
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
            snackBar.showSuccessSnack("Link copied")
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
    OrientationBox(
        portrait = { PortraitRedirectWalletContent(redirectState, platformTab, onPlatformTabSelect, wallet, onCopyLinkClick, onMobileRetry, onOpenPlayStore, onOpenWebApp) },
        landscape = { LandscapeRedirectContent(redirectState, platformTab, onPlatformTabSelect, wallet, onCopyLinkClick, onMobileRetry, onOpenPlayStore, onOpenWebApp) }
    )
}

@Composable
private fun PortraitRedirectWalletContent(
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
        WalletImageBox(
            platformTab = platformTab,
            redirectState = redirectState,
            wallet = wallet
        )
        VerticalSpacer(height = 8.dp)
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
                    wallet = wallet,
                    onCopyLinkClick = onCopyLinkClick,
                    onOpenWebApp = onOpenWebApp
                )
            }
        )
    }
}

@Composable
private fun LandscapeRedirectContent(
    redirectState: RedirectState,
    platformTab: PlatformTab,
    onPlatformTabSelect: (PlatformTab) -> Unit,
    wallet: Wallet,
    onCopyLinkClick: () -> Unit,
    onMobileRetry: () -> Unit,
    onOpenPlayStore: () -> Unit,
    onOpenWebApp: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (wallet.hasMobileWallet && wallet.hasWebApp) {
                PlatformTabRow(platformTab, onPlatformTabSelect)
            }
            VerticalSpacer(height = 16.dp)
            WalletImageBox(
                platformTab = platformTab,
                redirectState = redirectState,
                wallet = wallet
            )
            if (redirectState == RedirectState.NotDetected) {
                VerticalSpacer(height = 8.dp)
                StoreEntry(text = "Don't have ${wallet.name}?", onClick = onOpenPlayStore)
            }
            VerticalSpacer(height = 16.dp)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlatformBox(
                platformTab = platformTab,
                mobileWalletContent = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RedirectLabel(state = redirectState, wallet = wallet)
                        if (redirectState == RedirectState.NotDetected || redirectState == RedirectState.Reject) {
                            VerticalSpacer(height = 20.dp)
                            TryAgainButton(onClick = onMobileRetry)
                        }
                    }
                },
                webWalletContent = {
                    RedirectWebWalletScreen(
                        wallet = wallet,
                        onCopyLinkClick = onCopyLinkClick,
                        onOpenWebApp = onOpenWebApp
                    )
                }
            )
        }
    }
}

@Composable
private fun WalletImageBox(
    platformTab: PlatformTab,
    redirectState: RedirectState,
    wallet: Wallet
) {
    Box(
        modifier = Modifier.height(130.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            redirectState == RedirectState.NotDetected || platformTab == PlatformTab.WEB -> RoundedWalletImage(url = wallet.imageUrl)
            redirectState == RedirectState.Loading -> WalletImageWithLoader(url = wallet.imageUrl)
            redirectState == RedirectState.Reject -> RejectWalletImage(wallet.imageUrl)
        }
    }
}

@Composable
private fun RedirectLabel(state: RedirectState, wallet: Wallet) {
    val header: String
    val description: String
    val headerStyle: TextStyle
    val descriptionStyle: TextStyle
    when (state) {
        RedirectState.Loading -> {
            header = "Continue in ${wallet.name}"
            headerStyle = Web3ModalTheme.typo.paragraph500
            description = "Accept connection request in your wallet app"
            descriptionStyle = Web3ModalTheme.typo.small400.copy(color = Web3ModalTheme.colors.foreground.color200)
        }

        RedirectState.Reject -> {
            header = "Connection declined"
            description = "Connection can be declined if a previous request is still active"
            headerStyle = Web3ModalTheme.typo.paragraph400.copy(Web3ModalTheme.colors.error)
            descriptionStyle = Web3ModalTheme.typo.small400.copy(color = Web3ModalTheme.colors.foreground.color200)
        }

        RedirectState.NotDetected -> {
            header = "App not installed"
            description = String.Empty
            headerStyle = Web3ModalTheme.typo.paragraph400
            descriptionStyle = Web3ModalTheme.typo.small400.copy(color = Web3ModalTheme.colors.foreground.color200)
        }
    }
    Column(
        modifier = Modifier.padding(horizontal = 30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = header, style = headerStyle)
        if (description.isNotEmpty()) {
            VerticalSpacer(height = 8.dp)
            Text(
                text = description,
                style = descriptionStyle,
                textAlign = TextAlign.Center
            )
        }
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
    ) { redirectState ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (redirectState) {
                RedirectState.Loading -> LoadingState(state, wallet, onRetry, onCopyLinkClick)
                RedirectState.Reject -> RejectedState(state, wallet, onRetry)
                RedirectState.NotDetected -> NotDetectedWalletState(state, wallet, onOpenPlayStore)
            }
        }
    }
}

@Composable
private fun RedirectWebWalletScreen(
    wallet: Wallet,
    onCopyLinkClick: () -> Unit,
    onOpenWebApp: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Continue in ${wallet.name}", style = Web3ModalTheme.typo.paragraph400)
        VerticalSpacer(height = 8.dp)
        Text(
            text = "Accept connection request in the wallet",
            style = Web3ModalTheme.typo.small400.copy(color = Web3ModalTheme.colors.foreground.color200),
            textAlign = TextAlign.Center
        )
        VerticalSpacer(height = 20.dp)
        ChipButton(
            text = "Open",
            startIcon = {},
            endIcon = { ExternalIcon(size = 14.dp, tint = it) },
            style = ButtonStyle.ACCENT,
            size = ButtonSize.M,
            paddingValues = PaddingValues(horizontal = 12.dp),
            onClick = onOpenWebApp
        )
        VerticalSpacer(height = 20.dp)
        CopyActionEntry(onClick = onCopyLinkClick)
    }
}

private fun Wallet.toPlatform(): PlatformTab = when {
    hasMobileWallet -> PlatformTab.MOBILE
    hasWebApp -> PlatformTab.WEB
    else -> PlatformTab.MOBILE
}

@Composable
private fun LoadingState(
    redirectState: RedirectState,
    wallet: Wallet,
    onRetry: () -> Unit,
    onCopyLinkClick: () -> Unit
) {
    RedirectLabel(state = redirectState, wallet = wallet)
    VerticalSpacer(height = 20.dp)
    TryAgainButton(onClick = onRetry)
    VerticalSpacer(height = 20.dp)
    CopyActionEntry(onClick = onCopyLinkClick)
}

@Composable
private fun WalletImageWithLoader(url: String) {
    LoadingBorder(
        cornerRadius = 28.dp
    ) {
        RoundedWalletImage(url = url)
    }
}

@Composable
private fun RoundedWalletImage(url: String) {
    WalletImage(
        url = url, modifier = Modifier
            .size(80.dp)
            .border(width = 1.dp, color = Web3ModalTheme.colors.grayGlass10, shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
    )
}

@Composable
private fun RejectedState(
    state: RedirectState,
    wallet: Wallet,
    onRetry: () -> Unit
) {
    RedirectLabel(state = state, wallet = wallet)
    VerticalSpacer(height = 20.dp)
    TryAgainButton(onClick = onRetry)
}

@Composable
private fun NotDetectedWalletState(
    state: RedirectState,
    wallet: Wallet,
    onOpenPlayStore: () -> Unit
) {
    RedirectLabel(state = state, wallet = wallet)
    VerticalSpacer(height = 28.dp)
    StoreEntry(text = "Don't have ${wallet.name}?", onClick = onOpenPlayStore)
}

@Composable
private fun RejectWalletImage(url: String) {
    Box {
        RoundedWalletImage(url = url)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Web3ModalTheme.colors.background.color125, shape = CircleShape)
                .padding(2.dp)
        ) {
            DeclinedIcon()
        }
    }
}

@UiModePreview
@Landscape
@Composable
private fun PreviewRedirectWalletScreenWithLoadingState() {
    val wallet = testWallets.first()
    Web3ModalPreview(wallet.name) {
        RedirectWalletScreen(redirectState = RedirectState.Loading, platformTab = PlatformTab.MOBILE, {}, wallet, {}, {}, {}, {})
    }
}

@UiModePreview
@Landscape
@Composable
private fun PreviewRedirectWalletScreenWithRejectedState() {
    val wallet = testWallets.first()
    Web3ModalPreview(wallet.name) {
        RedirectWalletScreen(redirectState = RedirectState.Reject, platformTab = PlatformTab.MOBILE, {}, wallet, {}, {}, {}, {})
    }
}

@UiModePreview
@Landscape
@Composable
private fun PreviewRedirectWalletScreenWithNotDetectedState() {
    val wallet = testWallets.first()
    Web3ModalPreview(wallet.name) {
        RedirectWalletScreen(redirectState = RedirectState.NotDetected, platformTab = PlatformTab.MOBILE, {}, wallet, {}, {}, {}, {})
    }
}

@UiModePreview
@Landscape
@Composable
private fun PreviewRedirectWebWalletScreen() {
    val wallet = testWallets.first()
    Web3ModalPreview(wallet.name) {
        RedirectWalletScreen(redirectState = RedirectState.Loading, platformTab = PlatformTab.WEB, {}, wallet, {}, {}, {}, {})
    }
}
