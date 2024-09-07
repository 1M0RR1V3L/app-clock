package com.example.clock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.clock.MainActivity
import com.example.clock.R

class ChronometerService : Service() {

    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val channelId = "timer_service_channel"
    private val notificationId = 1
    private val handler = Handler()
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTimer()
            handler.postDelayed(this, 500) // Atualiza a cada meio segundo
        }
    }

    private val binder = TimerBinder()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
        return binder
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
        val channel = NotificationChannel(
            channelId,
            "Timer Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun updateTimer() {
        elapsedTime = SystemClock.elapsedRealtime() - startTime
        saveElapsedTime(elapsedTime)
        val minutes = (elapsedTime / 60000).toInt()
        val seconds = ((elapsedTime % 60000) / 1000).toInt()
        val intent = Intent("TIMER_UPDATED")
        intent.putExtra("elapsedTime", elapsedTime)
        sendBroadcast(intent)
    }


    private fun saveElapsedTime(time: Long) {
        val sharedPreferences = getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong("elapsedTime", time)
        editor.apply()
    }
    // No TimerService.kt
    fun getElapsedTime(): Long {
        return elapsedTime
    }

    fun startTimer() {
        startTime = SystemClock.elapsedRealtime() - elapsedTime
        handler.post(updateRunnable)
    }

    fun pauseTimer() {
        handler.removeCallbacks(updateRunnable)
    }

    fun stopTimer() {
        handler.removeCallbacks(updateRunnable)
        elapsedTime = 0
        saveElapsedTime(elapsedTime)
        val intent = Intent("TIMER_UPDATED")
        intent.putExtra("elapsedTime", elapsedTime)
        sendBroadcast(intent)
    }

    inner class TimerBinder : Binder() {
        fun getService(): ChronometerService = this@ChronometerService
    }
}
