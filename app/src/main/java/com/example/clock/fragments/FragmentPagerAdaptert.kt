package com.example.clock

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.clock.fragments.ChronometerFragment
import com.example.clock.fragments.TimerFragment
import com.example.clock.fragments.ClockFragment

class FragmentPagerAdapter(activity: MainActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 3 // Número de fragments
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChronometerFragment()
            1 -> TimerFragment()
            2 -> ClockFragment()
            else -> ClockFragment()
        }
    }
}
