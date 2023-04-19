package com.walletconnect.sample.dapp.web3modal.ui.common.widgets

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
import com.walletconnect.sample.dapp.web3modal.ui.theme.Web3ModalTheme

@Composable
fun MainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = Web3ModalTheme.colors.onMainColor
    val background = Web3ModalTheme.colors.mainColor
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
fun RoundedOutLineButton(
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
                color = Web3ModalTheme.colors.dividerColor,
                RoundedCornerShape(18.dp)
            )
            .padding(vertical = 8.dp, horizontal = 12.dp),
        startIcon = startIcon, endIcon = endIcon
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = Web3ModalTheme.colors.mainColor,
                textAlign = TextAlign.Center
            ),
        )
    }
}

@Composable
fun RoundedMainButton(
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
                Web3ModalTheme.colors.mainColor,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(vertical = 8.dp, horizontal = 12.dp),
        startIcon = startIcon, endIcon = endIcon
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = Web3ModalTheme.colors.onMainColor,
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
