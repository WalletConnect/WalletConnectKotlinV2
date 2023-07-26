package com.walletconnect.sample.web3inbox.ui.routes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.walletconnect.sample.common.ui.theme.WCSampleAppTheme

class W3ISampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WCSampleAppTheme {
                W3ISampleHost()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WCSampleAppTheme {
        W3ISampleHost()
    }
}