@file:OptIn(ExperimentalMaterialNavigationApi::class)

package com.walletconnect.sample.wallet.ui.routes.bottomsheet_routes.update_subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.navigation.material.BottomSheetNavigatorSheetState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.common.ui.theme.blue_accent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


@Composable
fun UpdateSubscriptionRoute(navController: NavController, sheetState: BottomSheetNavigatorSheetState, topic: String) {

    val viewModel: UpdateSubscriptionViewModel = viewModel()
    LaunchedEffect(Unit) { viewModel.setSubscriptionTopic(topic) }

    val notificationTypes by viewModel.notificationTypes.collectAsState()

    val onUpdateClick = {
        NotifyClient.update(
            Notify.Params.Update(topic, notificationTypes.filter { (_, value) -> value.second }.map { (name, _) -> name }),
            onSuccess = {
                CoroutineScope(Dispatchers.Main).launch {
                    navController.popBackStack()
                }
            },
            onError = {
                Timber.e(it.throwable)
            }
        )
    }

    val shape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp)

    LazyColumn(
        modifier = Modifier
            .clip(shape)
            .border(1.dp, ButtonDefaults.outlinedBorder.brush, shape)
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface)
            .padding(20.dp, 20.dp, 20.dp, 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
    ) {
        items(notificationTypes.toList()) { (name, setting) ->
            NotificationType(name = name, enabled = setting.second, description = setting.first, onClick = { (name, setting) ->
                viewModel.updateNotificationType(name, setting)
            })
        }
        item { UpdateButton(onUpdateClick) }
    }
}

@Composable
fun UpdateButton(onClick: () -> Unit) {
    Spacer(modifier = Modifier.height(40.dp))
    Button(modifier = Modifier.fillMaxWidth(), shape = CircleShape, onClick = onClick, colors = ButtonDefaults.buttonColors(backgroundColor = blue_accent)) {
        Text("Update", style = TextStyle(color = Color(0xFFFFFFFF)))
    }
}

@Composable
fun NotificationType(name: String, enabled: Boolean, description: String, onClick: (Pair<String, Pair<String, Boolean>>) -> Unit) {

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.fillMaxWidth(0.8f)) {
            Text(name, style = TextStyle(fontSize = 20.sp, color = MaterialTheme.colors.onSurface))
            Text(description, style = TextStyle(fontSize = 11.sp, color = MaterialTheme.colors.onSurface))
        }
        Switch(
            checked = enabled, onCheckedChange = { onClick(name to Pair(description, it)) },
            colors = SwitchDefaults.colors(checkedThumbColor = blue_accent, checkedTrackColor = blue_accent.copy(alpha = 0.5f))
        )
    }
}