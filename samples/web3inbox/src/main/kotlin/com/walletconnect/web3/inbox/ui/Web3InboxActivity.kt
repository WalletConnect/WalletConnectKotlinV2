package com.walletconnect.web3.inbox.ui

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.walletconnect.web3.inbox.ui.theme.Web3InboxTheme

class Web3InboxActivity : AppCompatActivity() {
    lateinit var webView: WebView
    lateinit var webViewClient: WebViewClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Declare a string that contains a url
        val mUrl = "https://web3inbox-dev-hidden-git-feat-pending-threads-ui-walletconnect1.vercel.app/login?chatProvider=android"//&account=eip155:1:123"
//        val mUrl = "https://google.com"//&account=eip155:1:123"

        setContent {
            Web3InboxTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    WebView(modifier = Modifier.fillMaxSize(), url = mUrl) { (_webView, _webViewClient) ->
                        webView = _webView
                        webViewClient = _webViewClient
                    }
                }
            }
        }
    }
}

fun formatJSScript(method: String, rpc: String): String {
    Log.d("KW3I", "$method, $rpc")
    return "console.log('$method', '$rpc')"
}

class AndroidBridge(val webView: WebView) {
    @JavascriptInterface
    fun postMessage(method: String, rpc: String) {
        when (method) {
            "getThreads", "getMessages" -> webView.post { webView.evaluateJavascript(formatJSScript(method, rpc), null) }
            else -> webView.post { webView.evaluateJavascript(formatJSScript("Unknown Method", ""), null) }
        }
    }
}


const val snippet = """this.web3inbox.chat.on('getMessages', ev => {
  console.log('Received getMessage', ev, 'posting to', ev.id)
  window.web3inbox.chat.postMessage(ev.id.toString(), {
    method: 'getMessages',
    result: [
      {
        topic: ev.params.topic,
        authorAccount: 'eip155:1:0x08e59B1456E70a1eDD3075c5e1104eE7040c6201',
        message: 'This is a dummy message',
        timestamp: Date.now()
      }
    ]
  })
})

this.web3inbox.chat.on('getThreads', ev => {
  const response = {
    method: 'getThreads',
    result: [
      {
        topic: 'notRealTopic',
        selfAccount: 'eip155:1:0x08e59B1456E70a1eDD3075c5e1104eE7040c6200',
        peerAccount: 'eip155:1:0x08e59B1456E70a1eDD3075c5e1104eE7040c6201'
      }
    ]
  }
  console.log('Received getThreads', ev, 'posting to', ev.id, 'sending', response)
  window.web3inbox.chat.postMessage(ev.id.toString(), response)
})"""

@Composable
fun WebView(modifier: Modifier = Modifier, url: String, onSetup: (Pair<WebView, WebViewClient>) -> Unit) {
    AndroidView(modifier = modifier, factory = {
        WebView(it).apply {
//            clearCache(true)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            webChromeClient = object : WebChromeClient() {

                override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                    Log.d(
                        "KW3I", "${message.message()} -- From line " +
                                "${message.lineNumber()} of ${message.sourceId()}"
                    )
                    return true
                }
            }
            val webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    evaluateJavascript(snippet, null)
                }

            }

            this.webViewClient = webViewClient

            settings.javaScriptEnabled = true
            addJavascriptInterface(AndroidBridge(this), "android")
            loadUrl(url)

            onSetup(this to webViewClient)
        }
    })
}