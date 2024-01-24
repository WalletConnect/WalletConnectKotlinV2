package com.walletconnect.sample.wallet.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.sample.wallet.ui.common.generated.ButtonWithLoader


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
            buttonColor = Color(0xFFD6D6D6),
            loaderColor = Color(0xFF000000),
            modifier = Modifier
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
                    text = "Confirm",
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