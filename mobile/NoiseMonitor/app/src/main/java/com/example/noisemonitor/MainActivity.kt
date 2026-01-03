package com.example.noisemonitor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.noisemonitor.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            openFragment(DashboardFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_monitor -> openFragment(DashboardFragment())
                R.id.nav_history -> openFragment(HistoryFragment())
                R.id.nav_alerts -> openFragment(AlertsFragment())
                R.id.nav_device -> openFragment(DeviceFragment())
                else -> false
            }
            true
        }
    }
}