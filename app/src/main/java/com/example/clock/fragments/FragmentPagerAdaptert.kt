package com.example.clock

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.clock.fragments.ChronometerFragment
import com.example.clock.fragments.TimerFragment

class FragmentPagerAdapter(activity: MainActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 2 // Número de fragments
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TimerFragment()
            1 -> ChronometerFragment()
            else -> TimerFragment()
        }
    }
}
