package com.alecocluc.musify.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var songAdapter: SongAdapter
    private lateinit var songDao: SongDao

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
                    previewUrl = it.previewUrl  // Provide a default value for previewUrl
                )
            }
            withContext(Dispatchers.Main) {
                songAdapter.updateSongs(savedSongs)
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
