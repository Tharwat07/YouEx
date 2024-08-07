package app.astra.youex

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class FloatingWindowService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isLongPress = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility",
        "UseCompatLoadingForDrawables", "InlinedApi"
    )
    override fun onCreate() {
        super.onCreate()
        startForegroundService()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_layout, null)

        // Set rounded corners background
        floatingView.background = resources.getDrawable(R.drawable.shape, null)

        params = WindowManager.LayoutParams(
            700,
            500,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FORMAT_CHANGED,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
        }

        windowManager.addView(floatingView, params)

        val webViewContainer = floatingView.findViewById<ViewGroup>(R.id.webViewContainer)
        val webView = WebViewSingleton.getWebView(this)
        webViewContainer.addView(webView)

        val closeButton = floatingView.findViewById<View>(R.id.closeButton)
        closeButton.setOnClickListener {
            stopSelf()
        }
        val movingButton = floatingView.findViewById<View>(R.id.moving)
        val fullButton = floatingView.findViewById<View>(R.id.full)

        fullButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            stopSelf() // Stop the FloatingWindowService
        }

        movingButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isLongPress = true
                    v.postDelayed({
                        if (isLongPress) {
                            // Start moving the window
                            isLongPress = true
                        }
                    }, 1) // Long press duration
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isLongPress) {
                        val deltaX = (event.rawX - initialTouchX).toInt()
                        val deltaY = (event.rawY - initialTouchY).toInt()

                        // Update params.x and params.y based on the direction of movement
                        params.x = initialX - deltaX
                        params.y = initialY - deltaY

                        // Ensure the view stays within the screen bounds
                        params.x = params.x.coerceIn(
                            0,
                            windowManager.defaultDisplay.width - floatingView.width
                        )
                        params.y = params.y.coerceIn(
                            0,
                            windowManager.defaultDisplay.height - floatingView.height
                        )

                        windowManager.updateViewLayout(floatingView, params)
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isLongPress = false
                    true
                }

                else -> false
            }
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val channelId = "floating_window_service"
        val channelName = "Floating Window Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Floating Window Service")
            .setContentText("Service is running in the background")
            .setSmallIcon(R.drawable.logo)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}