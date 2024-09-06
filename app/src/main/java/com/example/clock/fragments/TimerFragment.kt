package com.example.clock.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.clock.R
import com.example.clock.service.TimerService

class TimerFragment : Fragment() {

    private lateinit var timerTextView: TextView
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button

    private var isRunning = false
    private var timeInSeconds = 0
    private val handler = Handler()
    private var timerService: TimerService? = null
    private var isBound = false

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
            if (isRunning) {
                timeInSeconds++
                updateTimerText()
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)

        timerTextView = view.findViewById(R.id.timer_text_view)
        playButton = view.findViewById(R.id.start_timer_button)
        pauseButton = view.findViewById(R.id.pause_timer_button)
        stopButton = view.findViewById(R.id.stop_timer_button)

        // Restaurar estado salvo
        if (savedInstanceState != null) {
            timeInSeconds = savedInstanceState.getInt("timeInSeconds", 0)
            isRunning = savedInstanceState.getBoolean("isRunning", false)

            // Se o temporizador estava rodando antes de salvar, continua após restaurar
            if (isRunning) {
                handler.post(runnable)
            }
        }

        playButton.setOnClickListener {
            startTimer()
        }

        pauseButton.setOnClickListener {
            pauseTimer()
        }

        stopButton.setOnClickListener {
            stopTimer()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(context, TimerService::class.java)
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            requireActivity().unbindService(connection)
            isBound = false
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Salva o estado atual do temporizador
        outState.putInt("timeInSeconds", timeInSeconds)
        outState.putBoolean("isRunning", isRunning)
    }

    private fun startTimer() {
        if (!isRunning) {
            isRunning = true
            handler.post(runnable)
        }
    }

    private fun pauseTimer() {
        isRunning = false
        handler.removeCallbacks(runnable)
    }

    private fun stopTimer() {
        isRunning = false
        timeInSeconds = 0
        handler.removeCallbacks(runnable)
        updateTimerText()
    }

    private fun updateTimerText() {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        timerTextView.text = String.format("%02d:%02d", minutes, seconds)
    }
}
