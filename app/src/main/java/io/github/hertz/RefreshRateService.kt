package io.github.hertz

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.display.DisplayManager
import android.os.IBinder
import android.service.quicksettings.TileService
import android.text.TextPaint
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.preference.PreferenceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi

class RefreshRateService : LifecycleService() {

    lateinit var displayManager: DisplayManager
    lateinit var notificationManager: NotificationManagerCompat
    lateinit var refreshRateLiveData: LiveData<Int>

    private val iconMap = HashMap<Int, IconCompat>()
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    @ExperimentalCoroutinesApi
    override fun onCreate() {
        super.onCreate()

        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        notificationManager = NotificationManagerCompat.from(this)

        refreshRateLiveData = displayManager.refreshRateFlow().asLiveData()

        val notificationChannel = NotificationChannel(
            CHANNEL_ID, "RefreshRate",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(notificationChannel)

        textPaint.color = Color.WHITE
        textPaint.typeface = ResourcesCompat.getFont(this, R.font.digits)
        textPaint.textSize = 16 * resources.displayMetrics.density
        textPaint.letterSpacing = .05f

        startForeground(NOTIFICATION_ID, makeNotification(0))

        refreshRateLiveData.observe(this, { refreshRate ->
            notificationManager.notify(NOTIFICATION_ID, makeNotification(refreshRate))
        })

        ServiceState.started = true

        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putBoolean(PREF_KEY_REFRESH_RATE, true).apply()

        TileService.requestListeningState(this,
                ComponentName(this, RefreshRateTileService::class.java))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.getBooleanExtra(INTENT_EXTRA_NOTIFICATION_ACTION, false)!!) {
            stopSelf()
        }
        return START_NOT_STICKY;
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putBoolean(PREF_KEY_REFRESH_RATE, false).apply()
        ServiceState.started = false
        TileService.requestListeningState(this,
                ComponentName(this, RefreshRateTileService::class.java))
        super.onDestroy()
    }

    private fun makeNotification(refreshRate: Int): Notification {
        val serviceIntent = Intent(this, RefreshRateService::class.java)
        serviceIntent.putExtra(INTENT_EXTRA_NOTIFICATION_ACTION, true);

        val servicePendingIntent = PendingIntent.getService(this,
                0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder =
                NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(getSmallIcon(refreshRate))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSilent(true)
                        .setOngoing(true)
                        .setAutoCancel(false)

        notificationBuilder.addAction(NotificationCompat.Action(null,
                getString(R.string.stop_service), servicePendingIntent))

        return notificationBuilder.build()
    }

    private fun createSmallIcon(value: String): IconCompat {
        val width = textPaint.measureText(value)
        val height = -textPaint.ascent() + textPaint.descent()

        val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        canvas.drawText(value, 0f, height, textPaint)

        return IconCompat.createWithBitmap(bitmap)
    }

    private fun getSmallIcon(value: Int): IconCompat {
        if (!iconMap.containsKey(value)) {
            iconMap[value] = createSmallIcon(value.toString())
        }
        return iconMap[value] ?: createSmallIcon("0")
    }

    object ServiceState {
        var started: Boolean = false
    }
}