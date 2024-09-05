package com.example.clock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.clock.MainActivity
import com.example.clock.R

class ChronometerService : Service() {

    private var startTime: Long = 0
    private var pauseTime: Long = 0
    private var elapsedTime: Long = 0
    private var isRunning: Boolean = false
    private val binder = LocalBinder()
    private val channelId = "chronometer_service_channel"
    private val notificationId = 2

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun startChronometer() {
        if (!isRunning) {
            startTime = SystemClock.elapsedRealtime()
            isRunning = true
        }
    }

    fun pauseChronometer() {
        if (isRunning) {
            elapsedTime = SystemClock.elapsedRealtime() - startTime
            isRunning = false
        }
    }

    fun stopChronometer() {
        if (isRunning) {
            elapsedTime = SystemClock.elapsedRealtime() - startTime
            isRunning = false
        } else {
            elapsedTime = 0
        }
        startTime = 0
    }

    fun getElapsedTime(): Long {
        return if (isRunning) {
            SystemClock.elapsedRealtime() - startTime
        } else {
            elapsedTime
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)

        // Use FLAG_MUTABLE se o PendingIntent pode ser alterado ou FLAG_IMMUTABLE se não pode ser alterado
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE // Ou FLAG_MUTABLE, dependendo da necessidade
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Chronometer Service")
            .setContentText("Chronometer is running")
            .setSmallIcon(R.drawable.timer)
            .setContentIntent(pendingIntent)
            .build()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chronometer Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for chronometer service"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): ChronometerService = this@ChronometerService
    }
}
