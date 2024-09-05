package com.example.clock.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.clock.R

class ClockFragment : Fragment() {

    private lateinit var batteryLevelTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_clock, container, false)
        batteryLevelTextView = view.findViewById(R.id.battery_level_text)

        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context?.registerReceiver(null, batteryStatus)
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        batteryLevelTextView.text = getString(R.string.battery_level, level)

        return view
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context?.registerReceiver(batteryReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        context?.unregisterReceiver(batteryReceiver)
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            batteryLevelTextView.text = getString(R.string.battery_level, level)
        }
    }
}
