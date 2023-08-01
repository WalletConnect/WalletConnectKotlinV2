package com.walletconnect.wcmodal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.modal.ui.components.common.ClickableImage
import com.walletconnect.modal.ui.components.common.HorizontalSpacer
import com.walletconnect.modal.ui.components.common.VerticalSpacer
import com.walletconnect.wcmodal.R
import com.walletconnect.wcmodal.ui.preview.ComponentPreview
import com.walletconnect.wcmodal.ui.theme.ModalTheme

@Composable
internal fun ModalTopBar(
    title: String,
    endIcon: (@Composable () -> Unit)? = null,
    onBackPressed: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = ModalTheme.colors.textColor,
                fontSize = 20.sp
            ),
            modifier = Modifier.align(Alignment.Center)
        )
        onBackPressed?.let { onBackClick ->
            ClickableImage(
                tint = ModalTheme.colors.main,
                imageVector = ImageVector.vectorResource(id = R.drawable.chevron_left),
                contentDescription = "BackArrow",
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = onBackClick
            )
        }
        endIcon?.let {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                endIcon()
            }
        }
    }
}

@Composable
internal fun ModalSearchTopBar(
    searchValue: String,
    onSearchValueChange: (String) -> Unit,
    onBackPressed: (() -> Unit)
) {
    Box(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        ClickableImage(
            tint = ModalTheme.colors.main,
            imageVector = ImageVector.vectorResource(id = R.drawable.chevron_left),
            contentDescription = "BackArrow",
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onBackPressed
        )
        BasicTextField(
            value = searchValue,
            onValueChange = onSearchValueChange,
            textStyle = TextStyle(color = ModalTheme.colors.onBackgroundColor),
            cursorBrush = SolidColor(ModalTheme.colors.main),
            singleLine = true,
            modifier = Modifier
                .padding(horizontal = 30.dp)
                .fillMaxWidth()
                .align(Alignment.Center)
                .background(ModalTheme.colors.background)
                .border(width = 1.dp, color = ModalTheme.colors.main, shape = RoundedCornerShape(16.dp))
                .padding(2.dp),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalSpacer(width = 4.dp)
                    Icon(
                        tint = ModalTheme.colors.secondaryTextColor,
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_search),
                        contentDescription = "lens",
                    )
                    HorizontalSpacer(width = 8.dp)
                    Box {
                        if (searchValue.isBlank()) {
                            Text(text = "Search wallets", style = TextStyle(color = ModalTheme.colors.secondaryTextColor))
                        }
                        innerTextField()
                    }
                }
            },
        )

    }
}

@Preview
@Composable
private fun PreviewWeb3TopBar() {
    ComponentPreview {
        ModalTopBar(
            title = "Connect your wallet",
            endIcon = {
                ImageWithMainTint(icon = R.drawable.ic_scan)
            },
            onBackPressed = null
        )
        VerticalSpacer(height = 6.dp)
        ModalTopBar(
            title = "Scan the code",
            endIcon = {
                ImageWithMainTint(icon = R.drawable.ic_copy)
            },
            onBackPressed = {})
        ModalTopBar(
            title = "What is wallet?",
            onBackPressed = {}
        )
        ModalSearchTopBar(
            searchValue = "",
            onSearchValueChange = {},
            onBackPressed = {}
        )
        ModalSearchTopBar(
            searchValue = "Metamask",
            onSearchValueChange = {},
            onBackPressed = {}
        )
    }
}
