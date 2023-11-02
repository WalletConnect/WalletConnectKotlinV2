package com.walletconnect.sample.modal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AlertDialogRoute(navController: NavController, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colors.background, shape = RoundedCornerShape(32.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message, maxLines = 1, style = TextStyle(fontSize = 18.sp, color = MaterialTheme.colors.onBackground)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "OK", maxLines = 1, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colors.onBackground), modifier = Modifier
            .align(Alignment.End)
            .clickable { navController.popBackStack() })
    }
}