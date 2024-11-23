package com.alecocluc.musify

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.alecocluc.musify.databinding.ActivityMainBinding
import com.alecocluc.musify.fragments.LibraryFragment
import com.alecocluc.musify.fragments.SearchFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val theme = sharedPreferences.getString("theme", "default")
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el BottomNavigationView
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    setCurrentFragment(HomeFragment())
                    true
                }

                R.id.navigation_search -> {
                    setCurrentFragment(SearchFragment())
                    true
                }

                R.id.navigation_library -> {
                    setCurrentFragment(LibraryFragment())
                    true
                }

                else -> false
            }
        }
        setCurrentFragment(HomeFragment())
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.fragmentContainer.id, fragment)
            commit()
        }
    }
}
