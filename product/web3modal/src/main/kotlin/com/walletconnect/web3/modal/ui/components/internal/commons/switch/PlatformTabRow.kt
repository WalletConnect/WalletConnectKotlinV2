package com.walletconnect.web3.modal.ui.components.internal.commons.switch

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.MobileIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.WebIcon
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

internal enum class PlatformTab(
    val value: Int,
    val label: String
) {
    MOBILE(0, "Mobile"), WEB(1, "WebApp")
}

@Composable
internal fun rememberWalletPlatformTabs(initValue: PlatformTab = PlatformTab.MOBILE) = remember {
    mutableStateOf(initValue)
}

@Composable
internal fun PlatformTabRow(
    platformTab: PlatformTab,
    onPlatformTabSelect: (PlatformTab) -> Unit
) {
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        CustomIndicatorWithAnimation(tabPositions, platformTab)
    }
    val interactionSource = remember { MutableInteractionSource() }

    TabRow(
        selectedTabIndex = platformTab.value,
        backgroundColor = Color.Transparent,
        contentColor = Web3ModalTheme.colors.foreground.color200,
        indicator = indicator,
        divider = {},
        modifier = Modifier
            .width(250.dp)
            .height(40.dp)
            .border(width = 3.dp, color = Web3ModalTheme.colors.overlay02, shape = RoundedCornerShape(80f))
    ) {
        PlatformTab.values().forEach {
            val isSelected = platformTab == it
            Tab(
                modifier = Modifier.zIndex(5f),
                text = { TabContent(it, isSelected) },
                selected = isSelected,
                onClick = { onPlatformTabSelect(it) },
                interactionSource = interactionSource,
                selectedContentColor = Color.Transparent
            )
        }
    }
}

@Composable
private fun TabContent(platform: PlatformTab, isSelected: Boolean) {
    val color = if (isSelected) Web3ModalTheme.colors.foreground.color100 else Web3ModalTheme.colors.foreground.color200
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        PlatformTabIcon(platform = platform, tint = color)
        HorizontalSpacer(width = 4.dp)
        Text(text = platform.label, style = Web3ModalTheme.typo.small500.copy(color = color))
    }

}

@Composable
private fun CustomIndicatorWithAnimation(
    tabPositions: List<TabPosition>,
    platformState: PlatformTab
) {
    val transition = updateTransition(platformState.value, label = "")
    val indicatorStart by transition.animateDp(
        transitionSpec = {
            if (initialState < targetState) {
                spring(dampingRatio = 1f, stiffness = 50f)
            } else {
                spring(dampingRatio = 1f, stiffness = 1000f)
            }
        },
        label = "Indicator start animation"
    ) {
        tabPositions[it].left
    }

    val indicatorEnd by transition.animateDp(
        transitionSpec = {
            if (initialState < targetState) {
                spring(dampingRatio = 1f, stiffness = 1000f)
            } else {
                spring(dampingRatio = 1f, stiffness = 50f)
            }
        },
        label = "Indicator end animation"
    ) {
        tabPositions[it].right
    }

    Box(
        Modifier
            .offset(x = indicatorStart)
            .wrapContentSize(align = Alignment.BottomStart)
            .width(indicatorEnd - indicatorStart)
            .padding(2.dp)
            .fillMaxSize()
            .background(color = Web3ModalTheme.colors.overlay02, RoundedCornerShape(50))
            .border(BorderStroke(1.dp, Web3ModalTheme.colors.overlay02), RoundedCornerShape(50))
            .zIndex(1f)
    )
}


@Composable
private fun PlatformTabIcon(platform: PlatformTab, tint: Color) {
    when (platform) {
        PlatformTab.MOBILE -> MobileIcon(tint)
        PlatformTab.WEB -> WebIcon(tint)
    }
}


@UiModePreview
@Composable
private fun PlatformTabRowWebPreview() {
    ComponentPreview {
        PlatformTabRow(PlatformTab.WEB, {})
    }
}

@UiModePreview
@Composable
private fun PlatformTabRowMobilePreview() {
    ComponentPreview {
        PlatformTabRow(PlatformTab.MOBILE, {})
    }
}
