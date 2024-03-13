@file:OptIn(ExperimentalMaterialNavigationApi::class)

package com.walletconnect.sample.wallet.ui.routes.bottomsheet_routes.update_subscription

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.navigation.material.BottomSheetNavigatorSheetState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.sample.common.ui.theme.blue_accent
import com.walletconnect.sample.wallet.ui.routes.showSnackbar
import kotlinx.coroutines.launch


@Composable
fun UpdateSubscriptionRoute(navController: NavController, sheetState: BottomSheetNavigatorSheetState, topic: String) {

    val viewModel: UpdateSubscriptionViewModel = runCatching {
        viewModel<UpdateSubscriptionViewModel>(
            key = topic,
            factory = UpdateSubscriptionViewModelFactory(topic)
        )
    }.getOrElse {
        navController.popBackStack()
        return navController.showSnackbar("Active subscription no longer exists")
    }

    val notificationTypes by viewModel.notificationTypes.collectAsState()
    val activeSubscriptionsUI by viewModel.activeSubscriptionUI.collectAsState()
    val isUpdateEnabled by viewModel.isUpdateEnabled.collectAsState(false)
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

    val shape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp)

    LazyColumn(
        modifier = Modifier
            .offset(y = (1).dp)
            .clip(shape)
            .background(MaterialTheme.colors.background)
            .border(1.dp, ButtonDefaults.outlinedBorder.brush, shape)
            .fillMaxWidth()
    ) {

        item { Header(activeSubscriptionsUI.name, state) }
        items(notificationTypes.toList()) { (id, setting) ->
            NotificationType(state is UpdateSubscriptionState.Displaying, id = id, name = setting.first, checked = setting.third, description = setting.second, onClick = { (id, setting) ->
                viewModel.updateNotificationType(id, setting)
            })
        }
        item {
            UpdateButton(isUpdateEnabled) {
                viewModel.updateSubscription(onSuccess = {
                    scope.launch {
                        navController.popBackStack()
                    }
                }, onFailure = {
                    scope.launch {
                        Toast.makeText(navController.context, "Unable to update. Reason: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}

@Composable
fun Header(name: String, state: UpdateSubscriptionState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(6.dp))
        Spacer(
            modifier = Modifier
                .width(36.dp)
                .height(5.dp)
                .background(color = Color(0x667F7F7F), shape = RoundedCornerShape(size = 2.5.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.7f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Notification Preferences",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight(700),
                    )
                )
                Text(
                    text = "For $name",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colors.onSurface.copy(0.3f),
                    )
                )
            }
            AnimatedVisibility(modifier = Modifier.align(Alignment.CenterVertically), visible = state is UpdateSubscriptionState.Updating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = blue_accent)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
fun UpdateButton(isEnabled: Boolean, onClick: () -> Unit) {
    Button(
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 20.dp, vertical = 8.dp), shape = CircleShape, onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = blue_accent)
    ) {
        Text("Update", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight(600), color = Color(0xFFFFFFFF)))
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun NotificationType(isUpdateEnabled: Boolean, id: String, name: String, checked: Boolean, description: String, onClick: (Pair<String, Triple<String, String, Boolean>>) -> Unit) {

    Divider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.8f)) {
            Text(name, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight(500)))
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f), fontWeight = FontWeight(400)))
        }
        Switch(
            enabled = isUpdateEnabled,
            checked = checked, onCheckedChange = { onClick(id to Triple(name, description, it)) },
            colors = SwitchDefaults.colors(checkedThumbColor = blue_accent, checkedTrackColor = blue_accent.copy(alpha = 0.5f))
        )
    }
}
