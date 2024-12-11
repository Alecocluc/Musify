package com.alecocluc.musify.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alecocluc.musify.SettingsActivity
import com.alecocluc.musify.adapters.SongAdapter
import com.alecocluc.musify.database.SongDao
import com.alecocluc.musify.database.SongDatabase
import com.alecocluc.musify.databinding.FragmentLibraryBinding
import com.alecocluc.musify.models.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var songAdapter: SongAdapter
    private lateinit var songDao: SongDao

    private var startDateTime: Calendar? = null
    private var endDateTime: Calendar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.savedSongsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        songAdapter = SongAdapter(
            emptyList(),
            onFavoriteClick = { },
            onRemoveFavoriteClick = { song -> removeFromFavorites(song) },
            showSavedIcon = true
        )
        binding.savedSongsRecyclerView.adapter = songAdapter

        songDao = SongDatabase.getDatabase(requireContext()).songDao()
        loadSavedSongs()

        binding.settingsButtonLibrary.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.filterButtonLibrary.setOnClickListener {
            showDateTimePicker()
        }

        return view
    }

    private fun loadSavedSongs() {
        CoroutineScope(Dispatchers.IO).launch {
            val savedSongs = songDao.getAllSongs().map {
                Song(
                    id = it.id,
                    title = it.title,
                    artist = it.artist,
                    duration = it.duration,
                    coverUrl = it.coverUrl,
                    previewUrl = it.previewUrl,
                    savedAt = it.savedAt
                )
            }
            withContext(Dispatchers.Main) {
                songAdapter.updateSongs(savedSongs)
            }
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        // DatePicker para la fecha de inicio
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            startDateTime = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }

            // TimePicker para la hora de inicio
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                startDateTime?.apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }

                // DatePicker para la fecha de fin
                DatePickerDialog(requireContext(), { _, endYear, endMonth, endDayOfMonth ->
                    endDateTime = Calendar.getInstance().apply {
                        set(endYear, endMonth, endDayOfMonth)
                    }

                    // TimePicker para la hora de fin
                    TimePickerDialog(requireContext(), { _, endHourOfDay, endMinute ->
                        endDateTime?.apply {
                            set(Calendar.HOUR_OF_DAY, endHourOfDay)
                            set(Calendar.MINUTE, endMinute)
                            set(Calendar.SECOND, 59) // Al final del segundo
                        }
                        filterSongsByDateRange()
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun filterSongsByDateRange() {
        if (startDateTime == null || endDateTime == null) {
            Toast.makeText(requireContext(), "Por favor selecciona ambos intervalos de fecha y hora", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val songsByDateRange = songDao.getSongsByDateRange(
                startDateTime!!.timeInMillis,
                endDateTime!!.timeInMillis
            ).map {
                Song(
                    id = it.id,
                    title = it.title,
                    artist = it.artist,
                    duration = it.duration,
                    coverUrl = it.coverUrl,
                    previewUrl = it.previewUrl,
                    savedAt = it.savedAt
                )
            }

            withContext(Dispatchers.Main) {
                if (songsByDateRange.isEmpty()) {
                    Toast.makeText(requireContext(), "No se encontraron canciones en el rango seleccionado", Toast.LENGTH_SHORT).show()
                }
                songAdapter.updateSongs(songsByDateRange)
            }
        }
    }

    private fun removeFromFavorites(song: Song) {
        CoroutineScope(Dispatchers.IO).launch {
            songDao.delete(song.id)
            withContext(Dispatchers.Main) {
                val updatedList = songAdapter.songs.filter { it.id != song.id }
                songAdapter.updateSongs(updatedList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
