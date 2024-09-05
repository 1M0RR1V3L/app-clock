package com.example.clock.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.clock.R
import com.example.clock.service.ChronometerService

class ChronometerFragment : Fragment() {

    private lateinit var hoursMinutesView: TextView
    private lateinit var secondsView: TextView
    private lateinit var millisecondsView: TextView
    private lateinit var buttonPlay: ImageButton
    private lateinit var buttonPause: ImageButton
    private lateinit var buttonStop: ImageButton
    private lateinit var serviceIntent: Intent
    private var chronometerService: ChronometerService? = null
    private var isBound = false
    private val handler = Handler()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ChronometerService.LocalBinder
            chronometerService = binder.getService()
            isBound = true
            updateChronometer()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            chronometerService = null
            isBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chronometer, container, false)

        hoursMinutesView = view.findViewById(R.id.chronometer_hours_minutes)
        secondsView = view.findViewById(R.id.chronometer_seconds)
        millisecondsView = view.findViewById(R.id.chronometer_milliseconds)
        buttonPlay = view.findViewById(R.id.button_play)
        buttonPause = view.findViewById(R.id.button_pause)
        buttonStop = view.findViewById(R.id.button_stop)

        serviceIntent = Intent(activity, ChronometerService::class.java)
        activity?.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        buttonPlay.setOnClickListener {
            chronometerService?.startChronometer()
            startUpdatingUI()
        }
        buttonPause.setOnClickListener {
            chronometerService?.pauseChronometer()
        }
        buttonStop.setOnClickListener {
            chronometerService?.stopChronometer()
            updateChronometer()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isBound) {
            activity?.unbindService(connection)
            isBound = false
        }
        handler.removeCallbacksAndMessages(null)
    }

    private fun startUpdatingUI() {
        handler.post(object : Runnable {
            override fun run() {
                updateChronometer()
                handler.postDelayed(this, 10) // Atualiza a cada 10ms
            }
        })
    }

    private fun updateChronometer() {
        if (isBound) {
            val elapsedTime = chronometerService?.getElapsedTime() ?: 0
            val hours = (elapsedTime / 3600000).toInt()
            val minutes = (elapsedTime % 3600000 / 60000).toInt()
            val seconds = (elapsedTime % 60000 / 1000).toInt()
            val milliseconds = (elapsedTime % 1000 / 10).toInt()

            hoursMinutesView.text = String.format("%02d:%02d", hours, minutes)
            secondsView.text = String.format("%02d", seconds)
            millisecondsView.text = String.format("%03d", milliseconds)
        }
    }
}
