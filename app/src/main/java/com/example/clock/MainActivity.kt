package com.example.clock

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.clock.fragments.ClockFragment
import com.example.clock.fragments.ChronometerFragment
import com.example.clock.fragments.TimerFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        viewPager = findViewById(R.id.view_pager)

        // Ajustar padding do layout para insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configura o ViewPager2 com o FragmentPagerAdapter
        viewPager.adapter = FragmentPagerAdapter(this)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Atualiza a seleção do BottomNavigationView quando a página muda
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })

        // Configura o BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            vibrate(50) // Adiciona vibração ao clicar no item do BottomNavigationView
            when (item.itemId) {
                R.id.action_timer -> viewPager.currentItem = 0
                R.id.action_chronometer -> viewPager.currentItem = 1
                R.id.action_clock -> viewPager.currentItem = 2
                else -> viewPager.currentItem = 0 // Default case, shouldn't be reached
            }
            true
        }

        // Exibe o fragmento inicial
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.action_clock  // Define o item selecionado
            viewPager.currentItem = 2 // Define a página inicial no ViewPager2
        }

        // Configura o fullscreen
        setupFullScreen()
    }

    private fun setupFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun vibrate(milliseconds: Long) {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(milliseconds)
        }
    }
}
