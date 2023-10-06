package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = Web3ModalTheme.typo.paragraph500,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            style = Web3ModalTheme.typo.small500.copy(Web3ModalTheme.colors.foreground.color200),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun ColumnScope.NetworkBottomSection(
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
    VerticalSpacer(height = 4.dp)
    WhatIsNetworkLink(onClick = onClick)
}
@Composable
internal fun WhatIsNetworkLink(onClick: () -> Unit) {
    TransparentSurface(
        shape = RoundedCornerShape(70)
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuestionMarkIcon(tint = Web3ModalTheme.colors.main100, onClick = {})
            Text(
                text = "What is a network",
                style = Web3ModalTheme.typo.small600.copy(color = Web3ModalTheme.colors.main100),
                modifier = Modifier.padding(start = 0.dp, end = 8.dp)
            )
        }
    }
}
