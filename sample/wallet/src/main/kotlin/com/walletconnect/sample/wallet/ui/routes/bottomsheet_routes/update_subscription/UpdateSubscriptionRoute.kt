@file:OptIn(ExperimentalMaterialNavigationApi::class)

package com.walletconnect.sample.wallet.ui.routes.bottomsheet_routes.update_subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.navigation.material.BottomSheetNavigatorSheetState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
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
            Notify.Params.Update(topic, notificationTypes.filter { (_, value) -> value.third }.map { (name, _) -> name }),
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
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface)
            .padding(20.dp, 20.dp, 20.dp, 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
    ) {
        items(notificationTypes.toList()) { (id, setting) ->
            NotificationType(id = id, name = setting.first, enabled = setting.third, description = setting.second, onClick = { (id, setting) ->
                viewModel.updateNotificationType(id, setting)
            })
        }
        item { UpdateButton(onUpdateClick) }
    }
}

@Composable
fun UpdateButton(onClick: () -> Unit) {
    Spacer(modifier = Modifier.height(40.dp))
    Button(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Text("Update")
    }
}

@Composable
fun NotificationType(id: String, name: String, enabled: Boolean, description: String, onClick: (Pair<String, Triple<String, String, Boolean>>) -> Unit) {

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(0.8f)) {
            Text(name, style = TextStyle(fontSize = 20.sp, color = MaterialTheme.colors.onSurface))
            Text(description, style = TextStyle(fontSize = 11.sp, color = MaterialTheme.colors.onSurface))
        }
        Switch(modifier = Modifier.weight(0.2f), checked = enabled, onCheckedChange = { onClick(id to Triple(name, description, it)) })
    }
}