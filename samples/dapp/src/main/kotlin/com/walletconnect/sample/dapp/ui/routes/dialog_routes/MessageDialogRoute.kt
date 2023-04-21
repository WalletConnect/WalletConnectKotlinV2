package com.walletconnect.sample.dapp.ui.routes.dialog_routes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.walletconnect.sample_common.ui.themedColor

@Composable
fun MessageDialogRoute(
    navController: NavController,
    message: String
) {
    val backgroundColor = themedColor(Color(0xFF323234), Color(0xFFF2F2F7))
    val contentColor = themedColor(darkColor = 0xFF788686, lightColor = 0xFF788686)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            maxLines = 1,
            style = TextStyle(fontSize = 18.sp, color = contentColor)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "OK",
            maxLines = 1,
            style = TextStyle(
                fontWeight = FontWeight.Bold, fontSize = 22.sp, color = contentColor
            ),
            modifier = Modifier
                .align(Alignment.End)
                .clickable { navController.popBackStack() }
        )
    }
}