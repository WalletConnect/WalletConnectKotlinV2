package com.walletconnect.modals

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.walletconnect.modals.compose.ComposeActivity
import com.walletconnect.modals.kotlindsl.KotlinDSLActivity
import com.walletconnect.modals.navComponent.NavComponentActivity
import com.walletconnect.modals.ui.theme.WalletConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            WalletConnectTheme {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                ) {
                    Button(onClick = {
                        val intent = Intent(context, ComposeActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Text(text = "Jetpack Compose")
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(onClick = {
                        val intent = Intent(context, KotlinDSLActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Text(text = "Kotlin DSL")
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(onClick = {
                        val intent = Intent(context, NavComponentActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Text(text = "Navigation component")
                    }
                }
            }
        }
    }
}


