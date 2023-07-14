package com.walletconnect.sample.wallet.ui.routes.dialog_routes.snackbar_message

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.sample.common.ui.themedColor
import com.walletconnect.util.Empty

@Composable
fun SnackbarMessageRoute(navController: NavController, message: String?) {
    Snackbar(
        modifier = Modifier.padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 40.dp),
        action = {
            Button(onClick = {
                navController.popBackStack()
            }, colors = ButtonDefaults.buttonColors(
                backgroundColor = themedColor(darkColor = Color(0xFF9E9E9E), lightColor = Color(0xFF000000)),
            )) {
                Text("OK")
            }
        },
    ) {
        Text(text = message ?: String.Empty)
    }

}