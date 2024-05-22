package com.walletconnect.web3.modal.ui.utils

import android.webkit.WebView
import com.walletconnect.web3.modal.domain.magic.model.MagicRequest

internal const val WEB_APP_INTERFACE = "w3mWebview"
private const val SEND_MESSAGE = "sendMessage"

// window.addEventListener('message', function({ data }) { window.$WEB_APP_INTERFACE.postMessage(JSON.stringify(data)); });
//        const sendMessage = async (message) => { postMessage(message, '*'); };

//const iframeFL = document.getElementById('frame-mobile-sdk')
//
//      window.addEventListener('message', ({ data, origin }) => {
//        console.log('message received <=== ' + JSON.stringify({data,origin}))
//        window.w3mWebview.postMessage(JSON.stringify({data,origin}))
//      })
//
//      const sendMessage = async (message) => {
//        console.log('message posted =====> ' + JSON.stringify(message))
//        iframeFL.contentWindow.postMessage(message, '*')
//      }

private val SEND_MESSAGE_SCRIPT = """
    const iframeFL = document.getElementById('frame-mobile-sdk')
    
    console.log('FRAME =====> ' + iframeFL)
      
    window.addEventListener('message', ({ data, origin }) => {
        console.log('message received <=== ' + JSON.stringify({data,origin}))
        window.w3mWebview.postMessage(JSON.stringify({data,origin}))
    })    

    const sendMessage = async (message) => {
        console.log('message posted =====> ' + JSON.stringify(message))
        iframeFL.contentWindow.postMessage(message, '*')
    } 
""".trimIndent()

internal fun WebView.injectSendMessageScript() {
    evaluateJavascript(SEND_MESSAGE_SCRIPT, null)
}

internal fun WebView.sendMethod(request: MagicRequest) {
    evaluateJavascript("$SEND_MESSAGE($request)", null)
}