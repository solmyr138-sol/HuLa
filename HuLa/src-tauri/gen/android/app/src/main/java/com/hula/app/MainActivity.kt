package com.hula.app

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : TauriActivity() {

    private var splashHidden = false
    private var currentWebView: WebView? = null

    @Suppress("unused")
    fun show() {
        android.util.Log.i("MainActivity", "Splash show() called from Rust")
    }

    @Suppress("unused")
    fun hide() {
        android.util.Log.i("MainActivity", "Splash hide() called from Rust")
        runOnUiThread { hideStartupBackground() }
    }

    private fun hideStartupBackground() {
        if (!splashHidden) {
            splashHidden = true
            currentWebView?.let {
                it.setBackgroundColor(0xFFFFFFFF.toInt())
                window.setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val rootView: View = findViewById(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            if (imeVisible) {
                v.setPadding(0, 0, 0, imeHeight)
            } else {
                v.setPadding(0, 0, 0, 0)
            }
            insets
        }
        // Do NOT request permissions here — batch dialogs freeze emulator UI for 10s+.
        // Use scripts/android-grant-permissions.ps1 for dev, or request when a feature needs them.
    }

    override fun onWebViewCreate(webView: WebView) {
        super.onWebViewCreate(webView)
        currentWebView = webView
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.setBackgroundColor(0xFFFFFFFF.toInt())
        window.setBackgroundDrawableResource(R.drawable.launch_screen)
        splashHidden = false

        val chrome = webView.webChromeClient
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                chrome?.onProgressChanged(view, newProgress)
                if (newProgress >= 100) {
                    runOnUiThread { hideStartupBackground() }
                }
            }
        }

        // Fallback: native launch_screen is solid white — hide even if JS is slow on first Vite run
        webView.postDelayed({ runOnUiThread { hideStartupBackground() } }, 8000)
    }
}
