package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun HelpSection(
    title: String,
    body: String,
    assets: List<Int>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            assets.forEach { vectorRes ->
                Image(
                    imageVector = ImageVector.vectorResource(id = vectorRes),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .size(60.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title,
            style = Web3ModalTheme.typo.paragraph400,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            style = Web3ModalTheme.typo.small400.copy(Web3ModalTheme.colors.foreground.color200),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun NetworkBottomSection(
    onClick: () -> Unit
) {
    FullWidthDivider()
    VerticalSpacer(height = 12.dp)
    Text(
        text = "Your connected wallet may not support some of the networks available for this dApp",
        style = Web3ModalTheme.typo.small500.copy(color = Web3ModalTheme.colors.foreground.color300),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
    VerticalSpacer(height = 12.dp)
    WhatIsNetworkLink(onClick = onClick)
}
@Composable
internal fun WhatIsNetworkLink(onClick: () -> Unit) {
    TransparentSurface(
        shape = RoundedCornerShape(70)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuestionMarkIcon(tint = Web3ModalTheme.colors.accent100, modifier = Modifier.size(12.dp))
            HorizontalSpacer(width = 6.dp)
            Text(
                text = "What is a network",
                style = Web3ModalTheme.typo.small600.copy(color = Web3ModalTheme.colors.accent100)
            )
        }
    }
}

@Composable
@UiModePreview
private fun HelpSectionPreview() {
    ComponentPreview {
        HelpSection(
            title = "One login for all of web3",
            body = "Log in to any app by connecting your wallet. Say goodbye to countless passwords!",
            assets = listOf(R.drawable.login, R.drawable.profile, R.drawable.lock)
        )
    }
}

@Composable
@UiModePreview
private fun WhatIsNetworkLinkPreview() {
    ComponentPreview {
        WhatIsNetworkLink {}
    }
}
