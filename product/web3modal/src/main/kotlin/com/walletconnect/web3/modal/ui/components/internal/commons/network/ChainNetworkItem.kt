package com.walletconnect.web3.modal.ui.components.internal.commons.network

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.TransparentSurface
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun ChainNetworkItem(
    isSelected: Boolean,
    isEnabled: Boolean,
    networkName: String,
    imageUrl: String,
    onItemClick: () -> Unit,
) {
    val backgroundColor: Color
    val textColor: Color
    val borderColor: Color?
    when {
        isSelected -> {
            backgroundColor = Web3ModalTheme.colors.main005
            textColor = Web3ModalTheme.colors.main100
            borderColor = Web3ModalTheme.colors.main100
        }

        isEnabled -> {
            backgroundColor = Web3ModalTheme.colors.overlay02
            textColor = Web3ModalTheme.colors.foreground.color100
            borderColor = null
        }
        else -> {
            backgroundColor = Web3ModalTheme.colors.overlay10
            textColor = Web3ModalTheme.colors.overlay15
            borderColor = null
        }
    }
    TransparentSurface(
        modifier = Modifier.padding(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .width(76.dp)
                .height(96.dp)
                .background(backgroundColor)
                .clickable { onItemClick() }
                .padding(horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HexagonNetworkImage(
                url = imageUrl,
                isEnabled = isEnabled,
                borderColor = borderColor,
            )
            VerticalSpacer(height = 8.dp)
            Text(
                text = networkName,
                style = Web3ModalTheme.typo.tiny500.copy(textColor),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
