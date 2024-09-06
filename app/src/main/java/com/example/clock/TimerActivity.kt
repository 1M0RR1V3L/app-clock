package com.example.clock

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class TimerActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button

    private var isRunning = false
    private var timeInSeconds = 0
    private val handler = Handler()
    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                timeInSeconds++
                updateTimerText()
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        timerTextView = findViewById(R.id.timer_text_view)
        playButton = findViewById(R.id.start_timer_button)
        pauseButton = findViewById(R.id.pause_timer_button)
        stopButton = findViewById(R.id.stop_timer_button)

        // Restaurar estado salvo
        if (savedInstanceState != null) {
            timeInSeconds = savedInstanceState.getInt("timeInSeconds", 0)
            isRunning = savedInstanceState.getBoolean("isRunning", false)

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

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_timer -> {
                    // Não fazer nada, já estamos na TimerActivity
                    true
                }
  //              R.id.action_chronometer -> {
  //                  // Inicia a ChronometerActivity
  //                  val intent = Intent(this, ChronometerActivity::class.java)
 //                   startActivity(intent)
//                    true
 //               }
                R.id.action_clock -> {
                    // Inicia a MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
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
