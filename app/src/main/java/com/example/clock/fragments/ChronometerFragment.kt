package com.example.clock.fragments

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.clock.R
import com.example.clock.service.ChronometerService

class ChronometerFragment : Fragment() {

    private lateinit var chronometerTextView: TextView
    private lateinit var playButton: ImageButton
    private lateinit var pauseButton: ImageButton
    private lateinit var stopButton: ImageButton
    private lateinit var vibrator: Vibrator


    private var isRunning = false
    private var timeInSeconds = -1
    private val handler = Handler()
    private var chronomterService: ChronometerService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ChronometerService.TimerBinder
            chronomterService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            chronomterService = null
            isBound = false
        }
    }

    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                timeInSeconds++
                updateTimerText()
                handler.postDelayed(this, 1000) // Atualiza a cada 1 segundo
            }
        }
    }

    private fun restoreElapsedTime() {
        val sharedPreferences = requireContext().getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
        timeInSeconds = (sharedPreferences.getLong("elapsedTime", 0) / 1000).toInt()
        updateTimerText()
    }

    private fun vibrate(milliseconds: Long) {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(milliseconds)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chronometer, container, false)

        chronometerTextView = view.findViewById(R.id.chronometer_minutes)
        playButton = view.findViewById(R.id.button_play)
        pauseButton = view.findViewById(R.id.button_pause)
        stopButton = view.findViewById(R.id.button_stop)

        restoreElapsedTime()

        if (savedInstanceState != null) {
            timeInSeconds = savedInstanceState.getInt("timeInSeconds", 0)
            isRunning = savedInstanceState.getBoolean("isRunning", false)

            if (isRunning) {
                handler.post(runnable)
            }
        }

        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        playButton.setOnClickListener {
            startTimer()
            vibrate(50)

        }

        pauseButton.setOnClickListener {
            vibrate(50)
            pauseTimer()
        }

        stopButton.setOnClickListener {
            vibrate(50)
            stopTimer()
        }

        return view
    }

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "TIMER_UPDATED") {
                val elapsedTime = intent.getLongExtra("elapsedTime", 0)
                timeInSeconds = (elapsedTime / 1000).toInt()
                updateTimerText()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(context, ChronometerService::class.java)
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)

        val filter = IntentFilter("TIMER_UPDATED")
        requireContext().registerReceiver(timerReceiver, filter)

        if (isBound) {
            timeInSeconds = (chronomterService?.getElapsedTime() ?: 0L / 1000).toInt()
            updateTimerText()

            if (isRunning) {
                handler.post(runnable)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            requireActivity().unbindService(connection)
            isBound = false
        }
        requireContext().unregisterReceiver(timerReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("timeInSeconds", timeInSeconds)
        outState.putBoolean("isRunning", isRunning)
    }

    private fun startTimer() {
        if (!isRunning) {
            isRunning = true
            handler.post(runnable)
            chronomterService?.startTimer()
        }
    }

    private fun pauseTimer() {
        if (isRunning) {
            isRunning = false
            handler.removeCallbacks(runnable)
            chronomterService?.pauseTimer()
        }
    }

    private fun stopTimer() {
        isRunning = false
        timeInSeconds = 0
        handler.removeCallbacks(runnable)
        updateTimerText()
        clearElapsedTime()
        chronomterService?.stopTimer()

    }

    private fun updateTimerText() {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        chronometerTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun clearElapsedTime() {
        val sharedPreferences = requireContext().getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("elapsedTime")
        editor.apply()
    }
}
