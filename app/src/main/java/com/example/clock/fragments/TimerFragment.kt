package com.example.clock.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                timeInSeconds++
                updateTimerText()
                handler.postDelayed(this, 1000) // Atualiza a cada segundo
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

        playButton.setOnClickListener { startTimer() }
        pauseButton.setOnClickListener { pauseTimer() }
        stopButton.setOnClickListener { stopTimer() }

        return view
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(activity, TimerService::class.java)
        activity?.startService(serviceIntent)
    }

    override fun onStop() {
        super.onStop()
        val serviceIntent = Intent(activity, TimerService::class.java)
        activity?.stopService(serviceIntent)
    }

    private fun startTimer() {
        if (!isRunning) {
            isRunning = true
            handler.post(runnable)
        }
    }

    private fun pauseTimer() {
        isRunning = false
    }

    private fun stopTimer() {
        isRunning = false
        timeInSeconds = 0
        updateTimerText()
    }

    private fun updateTimerText() {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        timerTextView.text = String.format("%02d:%02d", minutes, seconds)
    }
}
