package com.walletconnect.modal.ui.components

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
import androidx.navigation.compose.rememberNavController
import com.walletconnect.modal.R
import com.walletconnect.modal.ui.navigation.Route
import com.walletconnect.modal.ui.preview.ComponentPreview
import com.walletconnect.modal.ui.theme.ModalTheme
import com.walletconnect.modalcore.ui.components.common.VerticalSpacer


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
internal fun QuestionMarkIconButton(navController: NavController) {
    val currentPath = navController.currentBackStackEntryAsState().value?.destination?.route
    val tint: Color
    val background: Color
    val onClick: () -> Unit
    if (currentPath == Route.Help.path) {
        tint = ModalTheme.colors.background
        background = ModalTheme.colors.onBackgroundColor
        onClick = { navController.popBackStack() }
    } else {
        tint = ModalTheme.colors.onBackgroundColor
        background = ModalTheme.colors.background
        onClick = { navController.navigate(Route.Help.path) }
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
        onClick = onClick
    )
}

@Composable
internal fun CloseIconButton(onClick: () -> Unit) {
    CircleButtonWithIcon(
        icon = {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
                contentDescription = "CloseIcon",
                colorFilter = ColorFilter.tint(ModalTheme.colors.onBackgroundColor)
            )
        },
        backgroundColor = ModalTheme.colors.background,
        onClick = onClick
    )
}

@Preview
@Composable
private fun CircleButtonsPreview() {
    ComponentPreview {
        CloseIconButton({})
        VerticalSpacer(4.dp)
        QuestionMarkIconButton(navController = rememberNavController())
    }
}
