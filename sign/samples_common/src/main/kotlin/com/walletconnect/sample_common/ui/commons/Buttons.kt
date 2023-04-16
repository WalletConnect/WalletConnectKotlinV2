package com.walletconnect.sample_common.ui.commons

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BlueButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = Color(0xFFE5E7E7)
    Button(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF3496ff),
            contentColor = contentColor
        ),
        onClick = {
            onClick()
        },
    ) {
        Text(text = text, color = contentColor)
    }
}
