package com.example.clock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.clock.MainActivity
import com.example.clock.R

class TimerService : Service() {

    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val channelId = "timer_service_channel"
    private val notificationId = 1
    private val handler = Handler()
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTimer()
            handler.postDelayed(this, 1000) // Atualiza a cada segundo
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (startTime == 0L) {
            startTime = SystemClock.elapsedRealtime()
        }

        val notification = createNotification("Timer is running")
        startForeground(notificationId, notification)
        handler.post(updateRunnable)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Timer Service")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.timer) // Certifique-se de que esse recurso existe
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Timer Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateTimer() {
        elapsedTime = SystemClock.elapsedRealtime() - startTime
        val minutes = (elapsedTime / 60000).toInt()
        val seconds = ((elapsedTime % 60000) / 1000).toInt()
        val timeText = String.format("%02d:%02d", minutes, seconds)
        val notification = createNotification("Timer running: $timeText")
        startForeground(notificationId, notification)
    }
}
