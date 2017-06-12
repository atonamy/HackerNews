package propertyguru.androidtest.com.hackernews.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import propertyguru.androidtest.com.hackernews.R

class WebviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        val url = intent.getStringExtra(EXTRA_URL)
        val webView = findViewById(R.id.webview) as WebView
        webView.setWebViewClient(WebViewClient())
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        title = url
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        webView.loadUrl(url)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        val EXTRA_URL = "extra.url"
    }
}