@file:OptIn(ExperimentalComposeUiApi::class)

package com.walletconnect.sample.wallet.ui.routes.dialog_routes.paste_uri

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.common.ui.themedColor

@Composable
fun PasteUriRoute(onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val backgroundColor = themedColor(Color(0xFF323234), Color(0xFFF2F2F7))
    val contentColor = themedColor(darkColor = 0xFF788686, lightColor = 0xFF788686)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(backgroundColor)
            .padding(horizontal = 5.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = "Enter a WalletConnect URI", maxLines = 1, style = TextStyle(
                fontWeight = FontWeight.Bold, fontSize = 22.sp, color = themedColor(Color(0xFFFFFFFF), Color(0xFF000000))
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row() {
            Text(text = "To get the URI press the ", color = contentColor, style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp))
            Icon(tint = contentColor, imageVector = ImageVector.vectorResource(id = R.drawable.ic_copy), contentDescription = "Paste Icon")
            Text(text = " copy to clipboard", color = contentColor, style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp))
        }
        Text(
            text = "button in wallet connection interfaces.",
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp, color = themedColor(darkColor = 0xFF788686, lightColor = 0xFF788686))
        )
        Spacer(modifier = Modifier.height(40.dp))
        val focusedColor = themedColor(Color(0xFFFFFFFF), Color(0xFF000000))
        val unfocusedColor = themedColor(Color(0xFF686868), Color(0xFF757575))
        val keyboardController = LocalSoftwareKeyboardController.current

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = focusedColor,
                focusedLabelColor = focusedColor,
                textColor = focusedColor,
                trailingIconColor = unfocusedColor,
                cursorColor = focusedColor,
                unfocusedBorderColor = unfocusedColor,
                unfocusedLabelColor = unfocusedColor,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSubmit(text)
                    keyboardController?.hide()
                }
            ),
            value = text,
            onValueChange = { text = it },
            label = { Text("wc://a13aef...") },
            maxLines = 1,
            trailingIcon = {
                IconButton(onClick = { onSubmit(text) }) {
                    Icon(Icons.Rounded.ArrowForward, contentDescription = "Submit")
                }
            }
        )
        Spacer(modifier = Modifier.height(40.dp))

    }
}