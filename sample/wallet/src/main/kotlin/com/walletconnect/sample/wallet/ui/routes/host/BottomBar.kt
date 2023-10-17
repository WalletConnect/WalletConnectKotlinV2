package com.walletconnect.sample.wallet.ui.routes.host

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.walletconnect.sample.common.ui.theme.blue_accent
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.ui.routes.Route

enum class BottomBarItem(val route: Route, val label: String, @DrawableRes val icon: Int) {
    CONNECTIONS(Route.Connections, "Connections", R.drawable.ic_connections),
    INBOX(Route.Inbox, "Inbox", R.drawable.ic_inbox),
    SETTINGS(Route.Settings, "Settings", R.drawable.ic_settings);

    companion object {
        val orderedList = listOf(CONNECTIONS, INBOX, SETTINGS)
    }
}

@Composable
internal fun rememberBottomBarMutableState(): MutableState<BottomBarState> {
    return remember { mutableStateOf(BottomBarState()) }
}

data class BottomBarState(
    val doesConnectionsItemHaveNotifications: Boolean = false,
    val doesInboxItemHaveNotifications: Boolean = false,
    val isDisplayed: Boolean = true,
)


@Composable
fun BottomBar(navController: NavController, state: BottomBarState, screens: Array<BottomBarItem> = BottomBarItem.values()) {
    Column() {
        Divider()
        BottomNavigation(backgroundColor = MaterialTheme.colors.background, elevation = 0.dp) {
            val currentRoute = currentRoute(navController)
            screens.forEach { screen ->
                val hasNotification = when (screen) {
                    BottomBarItem.CONNECTIONS -> state.doesConnectionsItemHaveNotifications
                    BottomBarItem.INBOX -> state.doesInboxItemHaveNotifications
                    else -> false
                }

                BottomNavigationItem(
                    label = { Text(screen.label) },
                    icon = { BottomNavIconWithBadge(screen.icon, hasNotification) },
                    selected = currentRoute == screen.route.path,
                    onClick = { navController.navigate(screen.route.path) }
                )
            }
        }
    }
}


@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Composable
fun BottomNavIconWithBadge(@DrawableRes icon: Int, hasNotification: Boolean) {
    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier.size(24.dp)  // You can adjust this size based on your icon size
    ) {
        Icon(painterResource(id = icon), contentDescription = null)

        if (hasNotification) {
            val color = blue_accent
            val bgColor = MaterialTheme.colors.background
            // Draw a red dot at the top-end of the icon as an indicator
            Canvas(modifier = Modifier.size(10.dp)) {
                drawCircle(color = bgColor)
                drawCircle(color = color, radius = 3.dp.toPx())
            }
        }
    }
}