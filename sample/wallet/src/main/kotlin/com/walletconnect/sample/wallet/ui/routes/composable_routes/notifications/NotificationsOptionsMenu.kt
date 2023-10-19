package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.sample.wallet.R

@Composable
fun NotificationsOptionsMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onMoreIconClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onNotificationSettings: () -> Unit,
    onUnsubscribe: () -> Unit,
) {
    val menuShape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .wrapContentSize(Alignment.TopStart)
            .clip(menuShape)
    ) {
        Icon(
            modifier = Modifier
                .size(20.dp)
                .clickable(indication = rememberRipple(bounded = false, radius = 20.dp), interactionSource = remember { MutableInteractionSource() }, onClick = onMoreIconClick),
            painter = painterResource(id = R.drawable.ic_more),
            contentDescription = "More",
        )
        CustomDropdownTheme(menuShape) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest,
                modifier = Modifier
                    .clip(menuShape)
                    .width(250.dp)
                    .background(color = MaterialTheme.colors.background)
                    .border(1.dp, ButtonDefaults.outlinedBorder.brush, menuShape)
                    .padding(horizontal = 1.dp)
            ) {
                DropdownMenuItem(onClick = {
                    onDismissRequest()
                    onNotificationSettings()
                }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Manage subscription",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight(400),
                                color = MaterialTheme.colors.onSurface,
                                textAlign = TextAlign.Center,
                            )
                        )
                        Icon(painter = painterResource(R.drawable.ic_notifications_settings), contentDescription = "Notifications settings")
                    }
                }
                Divider()
                DropdownMenuItem(onClick = {
                    onDismissRequest()
                    onUnsubscribe()
                }, modifier = Modifier.background(Color.Transparent)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Unsubscribe",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight(400),
                                color = Color(0xFFF05142),
                                textAlign = TextAlign.Center,
                            )
                        )
                        Icon(painter = painterResource(R.drawable.ic_unsubscribe), contentDescription = "Notifications settings", tint = Color(0xFFF05142))
                    }
                }
            }
        }
    }
}

@Composable
fun CustomDropdownTheme(mediumShape: CornerBasedShape, content: @Composable () -> Unit) {
    // Create a custom theme by copying the current theme and changing the surface color
    val customColors = MaterialTheme.colors.copy(surface = Color.Transparent)
    val customShapes = MaterialTheme.shapes.copy(medium = mediumShape)
    MaterialTheme(colors = customColors, shapes = customShapes, content = content)
}