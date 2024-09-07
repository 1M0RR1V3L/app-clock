package com.example.clock.fragments

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.clock.R
import com.example.clock.service.TimerService

class TimerFragment : Fragment() {

    private lateinit var timerTextView: TextView
    private lateinit var playButton: ImageButton
    private lateinit var pauseButton: ImageButton
    private lateinit var stopButton: ImageButton

    private var isRunning = false
    private var timeInSeconds = 0
    private val handler = Handler()
    private var timerService: TimerService? = null
    private var isBound = false
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var vibrator: Vibrator

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isBound = false
        }
    }

    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning && timeInSeconds > 0) {
                timeInSeconds--
                updateTimerText()
                handler.postDelayed(this, 1000) // Atualiza a cada segundo
            } else if (timeInSeconds == 0) {
                playAlarmSound()
                stopTimer() // Para o timer quando atinge zero
            }
        }
    }

    private fun playAlarmSound() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.nuclear_alarm)
        }
        mediaPlayer?.start()

        // Vibrar por 1 segundo
        vibrate(1000)
    }

    private fun vibrate(milliseconds: Long) {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(milliseconds)
        }
    }

    private fun restoreElapsedTime() {
        val sharedPreferences = requireContext().getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
        timeInSeconds = (sharedPreferences.getLong("elapsedTime", 0) / 1000).toInt()
        updateTimerText()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)

        timerTextView = view.findViewById(R.id.timer_input)
        playButton = view.findViewById(R.id.button_play)
        pauseButton = view.findViewById(R.id.button_pause)
        stopButton = view.findViewById(R.id.button_stop)

        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Configuração do TextView para entrada de tempo
        timerTextView.setOnClickListener {
            vibrate(50)
            showTimeInputDialog()
        }

        playButton.setOnClickListener {
            vibrate(50)
            startTimer()
        }

        pauseButton.setOnClickListener {
            vibrate(50)
            pauseTimer()
        }

        stopButton.setOnClickListener {
            vibrate(50)
            stopTimer()
        }

        restoreElapsedTime()

        if (savedInstanceState != null) {
            timeInSeconds = savedInstanceState.getInt("timeInSeconds", 0)
            isRunning = savedInstanceState.getBoolean("isRunning", false)

            if (isRunning) {
                handler.post(runnable)
            }
        }

        return view
    }

    private fun showTimeInputDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_time_input, null)
        val minutesEditText: EditText = dialogView.findViewById(R.id.edit_text_minutes)
        val secondsEditText: EditText = dialogView.findViewById(R.id.edit_text_seconds)

        AlertDialog.Builder(requireContext())
            .setTitle("Enter Time")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val minutes = minutesEditText.text.toString().toIntOrNull() ?: 0
                val seconds = secondsEditText.text.toString().toIntOrNull() ?: 0
                timeInSeconds = minutes * 60 + seconds
                updateTimerText()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        val intent = Intent(context, TimerService::class.java)
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)

        val filter = IntentFilter("TIMER_UPDATED")
        requireContext().registerReceiver(timerReceiver, filter)

        if (isBound) {
            timeInSeconds = (timerService?.getElapsedTime() ?: 0L / 1000).toInt()
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
        if (!isRunning && timeInSeconds > 0) {
            isRunning = true
            handler.post(runnable)
            timerService?.startTimer(timeInSeconds * 1000L) // Passar o tempo total em milissegundos
        }
    }

    private fun pauseTimer() {
        if (isRunning) {
            isRunning = false
            handler.removeCallbacks(runnable)
            timerService?.pauseTimer()
        }
    }

    private fun stopTimer() {
        isRunning = false
        timeInSeconds = 0
        handler.removeCallbacks(runnable)
        updateTimerText()
        clearElapsedTime()
        timerService?.stopTimer()
        playAlarmSound() // Toque o som ao parar o timer
    }

    private fun updateTimerText() {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        timerTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun clearElapsedTime() {
        val sharedPreferences = requireContext().getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("elapsedTime")
        editor.apply()
    }
}
