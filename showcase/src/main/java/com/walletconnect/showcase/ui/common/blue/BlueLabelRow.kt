package com.walletconnect.showcase.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun BlueLabelRow(values: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(start = 5.dp, top = 0.dp, end = 5.dp, bottom = 10.dp)) {
        values.forEach { value -> BlueLabelText(value) }
    }
}
