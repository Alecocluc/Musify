package com.alecocluc.musify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alecocluc.musify.adapters.AlbumAdapter
import com.alecocluc.musify.adapters.ArtistAdapter
import com.alecocluc.musify.adapters.PopularSongAdapter
import com.alecocluc.musify.databinding.FragmentHomeBinding
import com.alecocluc.musify.models.Album
import com.alecocluc.musify.models.Artist
import com.alecocluc.musify.models.Song
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var popularSongAdapter: PopularSongAdapter
    private lateinit var artistAdapter: ArtistAdapter
    private lateinit var albumAdapter: AlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        setupRecyclerViews()

        fetchPopularSongs()
        fetchPopularArtists()
        fetchPopularAlbums()

        return view
    }

    private fun setupRecyclerViews() {
        // Configuración del RecyclerView de canciones populares
        binding.popularSongsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        popularSongAdapter = PopularSongAdapter(emptyList()) { song ->
            // Acción al seleccionar una canción popular
            Toast.makeText(requireContext(), "Selected: ${song.title}", Toast.LENGTH_SHORT).show()
        }
        binding.popularSongsRecyclerView.adapter = popularSongAdapter

        // Configuración del RecyclerView de artistas populares
        binding.popularArtistsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        artistAdapter = ArtistAdapter(emptyList())
        binding.popularArtistsRecyclerView.adapter = artistAdapter

        // Configuración del RecyclerView de álbumes populares
        binding.popularAlbumsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        albumAdapter = AlbumAdapter(emptyList())
        binding.popularAlbumsRecyclerView.adapter = albumAdapter
    }

    private fun fetchPopularSongs() {
        val url = "https://api.deezer.com/chart"
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                CoroutineScope(Dispatchers.IO).launch {
                    val songs = parseSongs(response.getJSONObject("tracks").getJSONArray("data"))
                    withContext(Dispatchers.Main) {
                        popularSongAdapter.updateSongs(songs)
                    }
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(requireContext(), "Failed to fetch popular songs", Toast.LENGTH_SHORT).show()
            })
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun fetchPopularArtists() {
        val url = "https://api.deezer.com/chart"
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                CoroutineScope(Dispatchers.IO).launch {
                    val artists = parseArtists(response.getJSONObject("artists").getJSONArray("data"))
                    withContext(Dispatchers.Main) {
                        artistAdapter.updateArtists(artists)
                    }
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(requireContext(), "Failed to fetch popular artists", Toast.LENGTH_SHORT).show()
            })
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun fetchPopularAlbums() {
        val url = "https://api.deezer.com/chart"
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                CoroutineScope(Dispatchers.IO).launch {
                    val albums = parseAlbums(response.getJSONObject("albums").getJSONArray("data"))
                    withContext(Dispatchers.Main) {
                        albumAdapter.updateAlbums(albums)
                    }
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(requireContext(), "Failed to fetch popular albums", Toast.LENGTH_SHORT).show()
            })
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun parseSongs(jsonArray: JSONArray): List<Song> {
        val songs = mutableListOf<Song>()
        for (i in 0 until jsonArray.length()) {
            val songObject = jsonArray.getJSONObject(i)
            val song = Song(
                id = songObject.getLong("id"),
                title = songObject.getString("title"),
                artist = songObject.getJSONObject("artist").getString("name"),
                duration = songObject.getInt("duration"),
                coverUrl = songObject.getJSONObject("album").getString("cover_big"),
                previewUrl = songObject.getString("preview"),
                isFavorite = false
            )
            songs.add(song)
        }
        return songs
    }

    private fun parseArtists(jsonArray: JSONArray): List<Artist> {
        val artists = mutableListOf<Artist>()
        for (i in 0 until jsonArray.length()) {
            val artistObject = jsonArray.getJSONObject(i)
            val artist = Artist(
                id = artistObject.getLong("id"),
                name = artistObject.getString("name"),
                pictureUrl = artistObject.getString("picture_big")
            )
            artists.add(artist)
        }
        return artists
    }

    private fun parseAlbums(jsonArray: JSONArray): List<Album> {
        val albums = mutableListOf<Album>()
        for (i in 0 until jsonArray.length()) {
            val albumObject = jsonArray.getJSONObject(i)
            val album = Album(
                id = albumObject.getLong("id"),
                title = albumObject.getString("title"),
                artist = albumObject.getJSONObject("artist").getString("name"),
                coverUrl = albumObject.getString("cover_big")
            )
            albums.add(album)
        }
        return albums
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
