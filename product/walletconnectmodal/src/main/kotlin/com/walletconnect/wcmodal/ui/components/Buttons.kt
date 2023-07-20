package com.walletconnect.wcmodal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.walletconnect.wcmodal.ui.theme.ModalTheme

@Composable
internal fun MainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = ModalTheme.colors.onMainColor
    val background = ModalTheme.colors.main
    Button(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = background,
            contentColor = contentColor,
        ),
        onClick = {
            onClick()
        },
    ) {
        Text(text = text, color = contentColor)
    }
}

@Composable
internal fun RoundedOutLineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    startIcon: (@Composable () -> Unit)? = null,
    endIcon: (@Composable () -> Unit)? = null,
) {
    RoundedButton(
        onClick = onClick,
        modifier = modifier
            .border(
                width = 1.dp,
                color = ModalTheme.colors.dividerColor,
                RoundedCornerShape(18.dp)
            )
            .padding(vertical = 8.dp, horizontal = 12.dp),
        startIcon = startIcon, endIcon = endIcon
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = ModalTheme.colors.main,
                textAlign = TextAlign.Center
            ),
        )
    }
}

@Composable
internal fun RoundedMainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    startIcon: (@Composable () -> Unit)? = null,
    endIcon: (@Composable () -> Unit)? = null,
) {
    RoundedButton(
        onClick = onClick,
        modifier = modifier
            .background(
                ModalTheme.colors.main,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(vertical = 8.dp, horizontal = 12.dp),
        startIcon = startIcon, endIcon = endIcon
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = ModalTheme.colors.onMainColor,
                textAlign = TextAlign.Center
            ),
        )
    }
}

@Composable
private fun RoundedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    startIcon: (@Composable () -> Unit)? = null,
    endIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        startIcon?.let {
            startIcon()
            Spacer(modifier = Modifier.width(6.dp))
        }
        content()
        endIcon?.let {
            Spacer(modifier = Modifier.width(6.dp))
            endIcon()
        }
    }
}
