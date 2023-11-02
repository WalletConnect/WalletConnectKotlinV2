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
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.ui.components.internal.commons.TransparentSurface
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun ChainNetworkItem(
    image: Modal.Model.ChainImage,
    isSelected: Boolean,
    networkName: String,
    isEnabled: Boolean = true,
    onItemClick: () -> Unit,
) {
    val data = when (image) {
        is Modal.Model.ChainImage.Asset -> image.id
        is Modal.Model.ChainImage.Network -> image.url
    }
    val backgroundColor: Color
    val textColor: Color
    val borderColor: Color?
    when {
        isSelected -> {
            backgroundColor = Web3ModalTheme.colors.accent10
            textColor = Web3ModalTheme.colors.accent100
            borderColor = Web3ModalTheme.colors.accent100
        }

        isEnabled -> {
            backgroundColor = Web3ModalTheme.colors.grayGlass02
            textColor = Web3ModalTheme.colors.foreground.color100
            borderColor = null
        }

        else -> {
            backgroundColor = Web3ModalTheme.colors.grayGlass10
            textColor = Web3ModalTheme.colors.grayGlass15
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
                .clickable(isEnabled) { onItemClick() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HexagonNetworkImage(
                data = data,
                isEnabled = isEnabled,
                borderColor = borderColor,
            )
            VerticalSpacer(height = 8.dp)
            Text(
                text = networkName,
                style = Web3ModalTheme.typo.tiny500.copy(textColor),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@UiModePreview
@Composable
private fun ChainNetworkItemPreview() {
    val image = Modal.Model.ChainImage.Asset(R.drawable.system)
    MultipleComponentsPreview(
        { ChainNetworkItem(image = image, isSelected = true, isEnabled = true, networkName = "TestNetwork", onItemClick = {}) },
        { ChainNetworkItem(image = image, isSelected = false, isEnabled = true, networkName = "TestNetwork", onItemClick = {}) },
        { ChainNetworkItem(image = image, isSelected = false, isEnabled = false, networkName = "TestNetwork", onItemClick = {}) }
    )
}