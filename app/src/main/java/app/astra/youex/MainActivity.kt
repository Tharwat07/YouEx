package app.astra.youex

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 101

    @SuppressLint("MissingInflatedId", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_CODE)
        }

        val webViewContainer = findViewById<ViewGroup>(R.id.webViewContainer)
        val webView = WebViewSingleton.getWebView(this)
        webViewContainer.addView(webView)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Floating Action Button Clicked", Toast.LENGTH_SHORT).show()
                startService(Intent(this, FloatingWindowService::class.java))
                moveTaskToBack(true) // Hide MainActivity
            } else {
                Toast.makeText(this, "Overlay permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    fun getBase64FromDrawable(drawableId: Int): String {
        val bitmap = BitmapFactory.decodeResource(resources, drawableId)
        if (bitmap == null) {
            throw IllegalArgumentException("Drawable resource with ID $drawableId could not be decoded.")
        }
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}

@SuppressLint("StaticFieldLeak")
object WebViewSingleton {
    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    fun getWebView(context: Context): WebView {
        if (webView == null) {
            webView = WebView(context.applicationContext)
            webView?.settings?.javaScriptEnabled = true
            webView?.webViewClient = AdBlockWebViewClient(context)
            webView?.loadUrl("https://youtube.com")
        } else {
            (webView?.parent as? ViewGroup)?.removeView(webView)
        }
        return webView!!
    }
}

class AdBlockWebViewClient(private val context: Context) : WebViewClient() {
    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        if (url != null && (url.contains("ads") || url.contains("doubleclick") || url.contains("ad") || url.contains("googlesyndication"))) {
            return WebResourceResponse("text/plain", "utf-8", null)
        }
        return super.shouldInterceptRequest(view, url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        val base64Image = (context as MainActivity).getBase64FromDrawable(R.drawable.logo) // Replace with your drawable ID

        view?.loadUrl(
            "javascript:(function() { " +
                    "var adElements = document.querySelectorAll('[id^=\"ad\"][class^=\"ad\"]');" +
                    "for (var i = 0; i < adElements.length; i++) {" +
                    "adElements[i].style.display = 'none';" +
                    "}" +

                    "var logoElements = document.querySelectorAll('[id^=\"logo\"], .logo, [class*=\"logo\"], [class^=\"yt-logo\"]');" +
                    "for (var i = 0; i < logoElements.length; i++) {" +
                    "logoElements[i].style.display = 'none';" +
                    "}" +
                    "})()"
        )
    }
}

