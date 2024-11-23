package com.alecocluc.musify

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.alecocluc.musify.database.SongDatabase
import com.alecocluc.musify.databinding.ActivitySettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Aplicar tema al iniciar la actividad
        applyTheme()

        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val editor = sharedPreferences.edit()
            when (checkedId) {
                R.id.themeLight -> {
                    editor.putString("theme", "light")
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

                R.id.themeDark -> {
                    editor.putString("theme", "dark")
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }

                R.id.themeDefault -> {
                    editor.putString("theme", "default")
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            editor.apply()
            recreate() // Para aplicar el tema en directo
        }

        // Botón para borrar todas las canciones
        binding.btnClearSongs.setOnClickListener {
            deleteAllSongs()
        }

        // Botón para restablecer los ajustes predeterminados
        binding.btnResetSettings.setOnClickListener {
            resetSettings()
        }

        // Botón de regreso
        binding.backButton.setOnClickListener { navigateToMainActivity() }
    }

    private fun applyTheme() {
        val theme = sharedPreferences.getString("theme", "default")
        when (theme) {
            "light" -> {
                binding.themeLight.isChecked = true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            "dark" -> {
                binding.themeDark.isChecked = true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            else -> {
                binding.themeDefault.isChecked = true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun deleteAllSongs() {
        lifecycleScope.launch(Dispatchers.IO) {
            val database = SongDatabase.getDatabase(applicationContext)
            database.songDao().deleteAllSongs()
            launch(Dispatchers.Main) {
                Toast.makeText(
                    this@SettingsActivity,
                    "Todas las canciones han sido borradas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun resetSettings() {
        val editor = sharedPreferences.edit()
        editor.remove("theme")
        editor.apply()
        applyTheme()
        Toast.makeText(this, "Ajustes predeterminados restablecidos", Toast.LENGTH_SHORT).show()
        recreate()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
