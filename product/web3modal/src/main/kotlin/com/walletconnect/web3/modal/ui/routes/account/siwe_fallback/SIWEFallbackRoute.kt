package com.walletconnect.web3.modal.ui.routes.account.siwe_fallback

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletImageWithLoader
import com.walletconnect.web3.modal.ui.routes.connect.ConnectViewModel
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun SIWEFallbackRoute(
    connectViewModel: ConnectViewModel
) {
    val isConfirmLoading = connectViewModel.isConfirmLoading.collectAsState()
    val isCancelLoading = connectViewModel.isCancelLoading.collectAsState()
    SIWEFallback(
        connectViewModel,
        isLoadingConfirm = isConfirmLoading.value,
        isLoadingCancel = isCancelLoading.value,
        onConfirm = { connectViewModel.sendSIWEOverPersonalSign() },
        onCancel = { connectViewModel.disconnect() }
    )
}

@Composable
private fun SIWEFallback(
    connectViewModel: ConnectViewModel,
    isLoadingConfirm: Boolean,
    isLoadingCancel: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VerticalSpacer(20.dp)
        WalletImageWithLoader(connectViewModel.wallet?.imageUrl)
        VerticalSpacer(4.dp)
        Text(
            text = connectViewModel.wallet?.name ?: "",
            style = Web3ModalTheme.typo.paragraph400,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(20.dp)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Web3Modal needs to connect to your wallet",
            style = Web3ModalTheme.typo.paragraph400,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign this message to prove you own this wallet and proceed.\\n Cancelling will disconnect you.",
            style = Web3ModalTheme.typo.small400.copy(Web3ModalTheme.colors.foreground.color200),
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(20.dp)
        Buttons(
            allowButtonColor = Color(0xFF4A90E2),
            onCancel = { onCancel() },
            onConfirm = { onConfirm() },
            isLoadingCancel = isLoadingCancel,
            isLoadingConfirm = isLoadingConfirm
        )
        VerticalSpacer(20.dp)
    }
}

@Composable
fun Buttons(
    allowButtonColor: Color,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit = {},
    isLoadingConfirm: Boolean,
    isLoadingCancel: Boolean
) {
    Row(modifier = modifier) {
        Spacer(modifier = Modifier.width(18.dp))
        ButtonWithLoader(
            buttonColor = Color(0xFFFFFFFF),
            loaderColor = Color(0xFF000000),
            modifier = Modifier
                .border(width = 1.dp, color = Web3ModalTheme.colors.grayGlass05, shape = RoundedCornerShape(20.dp))
                .weight(1f)
                .height(46.dp)
                .clickable { onCancel() },
            isLoading = isLoadingCancel,
            content = {
                Text(
                    text = "Cancel",
                    style = TextStyle(
                        fontSize = 20.0.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF000000),
                    ),
                    modifier = modifier.wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
        )
        Spacer(modifier = Modifier.width(12.dp))
        ButtonWithLoader(
            buttonColor = allowButtonColor,
            loaderColor = Color(0xFFFFFFFF),
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clickable { onConfirm() },
            isLoadingConfirm,
            content = {
                Text(
                    text = "Sign",
                    style = TextStyle(
                        fontSize = 20.0.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(
                            alpha = 255,
                            red = 255,
                            green = 255,
                            blue = 255
                        ),
                    ),
                    modifier = modifier.wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
        )
        Spacer(modifier = Modifier.width(20.dp))
    }
}

@Composable
fun ButtonWithLoader(
    buttonColor: Color,
    loaderColor: Color,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    ButtonTopLevel(buttonColor, modifier = modifier) {
        Button(isLoading = isLoading, content = content, loaderColor = loaderColor)
    }
}

@Composable
fun Button(modifier: Modifier = Modifier, isLoading: Boolean, content: @Composable () -> Unit, loaderColor: Color) {
    AnimatedContent(targetState = isLoading, label = "Loading") { state ->
        if (state) {
            CircularProgressIndicator(
                modifier = modifier
                    .size(48.dp)
                    .padding(8.dp)
                    .wrapContentWidth(align = Alignment.CenterHorizontally)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                color = loaderColor, strokeWidth = 4.dp
            )
        } else {
            content()
        }
    }
}

@Composable
fun ButtonTopLevel(
    buttonColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(
                start = 0.0.dp,
                top = 0.0.dp,
                end = 0.0.dp,
                bottom = 0.0.dp
            )
            .clip(RoundedCornerShape(20.dp))
            .background(buttonColor)
            .fillMaxWidth(1.0f)
            .fillMaxHeight(1.0f)
    ) {
        content()
    }
}