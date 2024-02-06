package com.walletconnect.web3.modal.ui.components.internal.email.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.MutableContextWrapper
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.webkit.WebViewClientCompat
import com.walletconnect.foundation.util.Logger
import timber.log.Timber
import java.lang.IllegalStateException

const val SECURE_WEBSITE_URL = "https://secure.walletconnect.com"

internal class EmailWebViewWrapper(
    context: Context,
    private val logger: Logger
) {
    val mutableContext = MutableContextWrapper(context)

    private val webView = WebView(mutableContext)
    private var webViewDialog: WebViewDialog? = null

    init {
        WebView.setWebContentsDebuggingEnabled(true)
        if (context is Application) {
            initWebView()
        } else {
            throw IllegalStateException("Invalid context")
        }
    }

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    private fun initWebView() {
        // Setup settings
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        // Setup webView
        webView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        webView.webChromeClient = EmailChromeClient(logger)
        webView.webViewClient = EmailWebView()
        webView.addJavascriptInterface(this, "FortmaticAndroid")
        webView.loadUrl(SECURE_WEBSITE_URL)
    }

    fun newActivityContext(context: Context) {
        mutableContext.baseContext = context
    }

    /**
     * Webview display related
     */
    private fun showOverlay() {
        runOnUiThread {
            if (it is Activity) {
                webView.visibility = View.VISIBLE
                webViewDialog = WebViewDialog(it, webView)
                webViewDialog?.show()
            } else {

            }
        }
    }

    private fun hideOverlay() {
        runOnUiThread {
            if (webView.parent != null) {
                val vg = (webView.parent as ViewGroup)
                vg.removeView(webView)
            }
            webView.visibility = View.INVISIBLE
            webViewDialog?.dismiss()
        }
    }

    private fun runOnUiThread(cb: (Context) -> Unit) {
        val ctx = mutableContext.baseContext
        if (ctx is Application) {
            val handler = Handler(ctx.mainLooper)
            handler.post {
                cb(ctx)
            }
        }
        if (ctx is Activity) {
            /* Only the original thread that created a view hierarchy can touch its views. */
            ctx.runOnUiThread {
                run {
                    cb(ctx)
                }
            }
        }
    }
}

private class EmailChromeClient(
    private val logger: Logger
) : WebChromeClient() {

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        logger.log(consoleMessage?.message())
        return super.onConsoleMessage(consoleMessage)
    }

}

private class EmailWebView : WebViewClientCompat() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        //TODO
        return super.shouldOverrideUrlLoading(view, request)
    }
}

private class WebViewDialog(context: Context, val webView: WebView) : Dialog(context) {

    init {
        setContentView(createContentView())
        setCancelable(true)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    private fun createContentView(): View {
        val layout = RelativeLayout(context)
        if (webView.parent != null) {
            (webView.parent as ViewGroup).removeView(webView)
        }
        layout.addView(webView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return layout
    }

}