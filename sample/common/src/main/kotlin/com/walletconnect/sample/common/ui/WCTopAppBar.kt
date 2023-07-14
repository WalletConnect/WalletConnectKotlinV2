package com.walletconnect.sample.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.sample.common.BuildConfig
import com.walletconnect.sample.common.R

@Composable
fun WCTopAppBar(
    modifier: Modifier = Modifier,
    titleText: String,
    versionText: String = BuildConfig.BOM_VERSION,
    titleStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        color = themedColor(darkColor = 0xFFE5E7E7, lightColor = 0xFF141414)
    ),
    versionStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 11.sp,
        color = themedColor(darkColor = 0xFFE5E7E7, lightColor = 0xFF141414)
    ),
    onBackIconClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(32.dp))
        onBackIconClick?.let {
            Icon(
                tint = Color(0xFF3496ff),
                imageVector = ImageVector.vectorResource(id = R.drawable.chevron_left),
                contentDescription = "BackArrow",
                modifier = Modifier.clickable { onBackIconClick() }
            )
            Spacer(modifier = Modifier.width(32.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(text = titleText, style = titleStyle)
            Text(text = versionText, style = versionStyle)
        }
    }
}
