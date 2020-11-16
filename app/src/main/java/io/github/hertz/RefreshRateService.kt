package io.github.hertz

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.IBinder
import android.service.quicksettings.TileService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.preference.PreferenceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi

class RefreshRateService : LifecycleService() {

    lateinit var displayManager: DisplayManager
    lateinit var notificationManager: NotificationManagerCompat
    lateinit var refreshRateLiveData: LiveData<Int>

    @ExperimentalCoroutinesApi
    override fun onCreate() {
        super.onCreate()

        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        notificationManager = NotificationManagerCompat.from(this)

        refreshRateLiveData = displayManager.refreshRateFlow().asLiveData()

        val notificationChannel = NotificationChannel(CHANNEL_ID, "RefreshRate",
            NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(notificationChannel)

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

    object ServiceState {
        var started: Boolean = false
    }
}