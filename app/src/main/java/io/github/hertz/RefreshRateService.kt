package io.github.hertz

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import kotlin.math.roundToInt

const val NOTIFICATION_ID = 9000;
const val CHANNEL_ID = "rr_channel"

class RefreshRateService : Service() {

    lateinit var displayManager: DisplayManager
    lateinit var notificationManager: NotificationManagerCompat

    var refreshRate: Int = 0

    override fun onCreate() {
        super.onCreate()

        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        notificationManager = NotificationManagerCompat.from(this)

        refreshRate = displayManager.displays[0].refreshRate.roundToInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "RefreshRate",
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        displayManager.registerDisplayListener(displayListener, null)

        startForeground(NOTIFICATION_ID, makeNotification())

        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putBoolean(PREF_KEY_REFRESH_RATE, true).apply()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra(INTENT_EXTRA_NOTIFICATION_ACTION, false)!!) {
            stopSelf()
        }
        return START_NOT_STICKY;
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        displayManager.unregisterDisplayListener(displayListener)
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putBoolean(PREF_KEY_REFRESH_RATE, false).apply()
        super.onDestroy()
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(p0: Int) {
        }

        override fun onDisplayChanged(displayId: Int) {
            refreshRate = displayManager.getDisplay(displayId).refreshRate.roundToInt()
            notificationManager.notify(NOTIFICATION_ID, makeNotification())
        }

        override fun onDisplayRemoved(p0: Int) {
        }
    }

    private fun makeNotification(): Notification {
        val serviceIntent = Intent(this, RefreshRateService::class.java)
        serviceIntent.putExtra(INTENT_EXTRA_NOTIFICATION_ACTION, true);

        val servicePendingIntent = PendingIntent.getService(this,
                0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val smallIconId : Int = when (refreshRate) {
            60 -> R.drawable.ic_refresh_rate_60
            90 -> R.drawable.ic_refresh_rate_90
            120 -> R.drawable.ic_refresh_rate_120
            144 -> R.drawable.ic_refresh_rate_144
            else -> 0
        }

        val notificationBuilder=
                NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(smallIconId)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setNotificationSilent()
                        .setOngoing(true)
                        .setAutoCancel(false)

        notificationBuilder.addAction(NotificationCompat.Action(null,
            getString(R.string.stop_service), servicePendingIntent))

        return notificationBuilder.build()
    }

}