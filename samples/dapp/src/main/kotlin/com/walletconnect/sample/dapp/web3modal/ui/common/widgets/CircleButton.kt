package com.walletconnect.sample.dapp.web3modal.ui.common.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.walletconnect.sample.dapp.R
import com.walletconnect.sample.dapp.web3modal.ui.Route
import com.walletconnect.sample.dapp.web3modal.ui.theme.Web3ModalTheme
import com.walletconnect.sample_common.ui.theme.PreviewTheme


@Composable
private fun CircleButtonWithIcon(
    icon: @Composable () -> Unit,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .shadow(elevation = 4.dp, shape = CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
fun QuestionMarkIconButton(navController: NavController) {
    val currentPath = navController.currentBackStackEntryAsState().value?.destination?.route ?: ""
    val tint: Color
    val background: Color
    if (currentPath == Route.Help.path) {
        tint = Web3ModalTheme.colors.background
        background = Web3ModalTheme.colors.onBackgroundColor
    } else {
        tint = Web3ModalTheme.colors.onBackgroundColor
        background = Web3ModalTheme.colors.background
    }
    CircleButtonWithIcon(
        icon = {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_question_mark),
                contentDescription = "QuestionMark",
                colorFilter = ColorFilter.tint(tint)
            )
        },
        backgroundColor = background,
        onClick = { navController.navigate(Route.Help.path) }
    )
}

@Composable
fun CloseIconButton(onClick: () -> Unit) {
    CircleButtonWithIcon(
        icon = {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
                contentDescription = "CloseIcon",
                colorFilter = ColorFilter.tint(Web3ModalTheme.colors.onBackgroundColor)
            )
        },
        backgroundColor = Web3ModalTheme.colors.background,
        onClick = onClick
    )
}

@Preview
@Composable
private fun CircleButtonsPreview() {
    PreviewTheme {
        CloseIconButton({})
    }
}
