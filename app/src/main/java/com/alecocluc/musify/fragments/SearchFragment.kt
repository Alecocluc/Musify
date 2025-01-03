package com.alecocluc.musify.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alecocluc.musify.SettingsActivity
import com.alecocluc.musify.adapters.SongAdapterSearch
import com.alecocluc.musify.database.SongDao
import com.alecocluc.musify.database.SongDatabase
import com.alecocluc.musify.database.SongEntity
import com.alecocluc.musify.databinding.FragmentSearchBinding
import com.alecocluc.musify.models.Song
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.util.*

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var requestQueue: RequestQueue
    private lateinit var songAdapterSearch: SongAdapterSearch
    private lateinit var songDao: SongDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root

        requestQueue = Volley.newRequestQueue(requireContext())
        songDao = SongDatabase.getDatabase(requireContext()).songDao()

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicialización del SongAdapterSearch
        songAdapterSearch = SongAdapterSearch(
            emptyList(),
            { song -> addToFavorites(song) },
            { song -> removeFromFavorites(song) }
        )

        binding.recyclerView.adapter = songAdapterSearch

        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            searchSongs(query)
        }

        binding.settingsButtonSearch.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    private fun searchSongs(query: String) {
        val url = "https://api.deezer.com/search?q=$query"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val dataArray = response.getJSONArray("data")
                    val songs = mutableListOf<Song>()
                    CoroutineScope(Dispatchers.IO).launch {
                        val favoriteSongsIds = songDao.getAllSongs().map { it.id }.toSet()
                        for (i in 0 until dataArray.length()) {
                            val songObject = dataArray.getJSONObject(i)
                            val song = Song(
                                id = songObject.getLong("id"),
                                title = songObject.getString("title"),
                                artist = songObject.getJSONObject("artist").getString("name"),
                                duration = songObject.getInt("duration"),
                                coverUrl = songObject.getJSONObject("album").getString("cover_big"),
                                previewUrl = songObject.getString("preview"), // Extract preview URL
                                isFavorite = songObject.getLong("id") in favoriteSongsIds
                            )
                            songs.add(song)
                        }
                        withContext(Dispatchers.Main) {
                            songAdapterSearch.updateSongs(songs)
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(requireContext(), "Error al buscar canciones", Toast.LENGTH_SHORT)
                    .show()
            })

        requestQueue.add(request)
    }

    private fun addToFavorites(song: Song) {
        val songEntity = SongEntity(
            id = song.id,
            title = song.title,
            artist = song.artist,
            duration = song.duration,
            coverUrl = song.coverUrl,
            previewUrl = song.previewUrl,
            savedAt = System.currentTimeMillis()
        )

        CoroutineScope(Dispatchers.Main).launch {
            songAdapterSearch.updateFavoriteStatus(song.id, true)
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (!songDao.isSongFavorite(song.id)) {
                songDao.insert(songEntity)
            }
        }
    }

    private fun removeFromFavorites(song: Song) {
        CoroutineScope(Dispatchers.Main).launch {
            songAdapterSearch.updateFavoriteStatus(song.id, false)
        }

        CoroutineScope(Dispatchers.IO).launch {
            songDao.delete(song.id)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
