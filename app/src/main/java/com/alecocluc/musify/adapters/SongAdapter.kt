package com.alecocluc.musify.adapters

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alecocluc.musify.R
import com.alecocluc.musify.databinding.ItemSongBinding
import com.alecocluc.musify.models.Song
import com.squareup.picasso.Picasso

class SongAdapter(
    var songs: List<Song>,
    private val onFavoriteClick: (Song) -> Unit,
    private val onRemoveFavoriteClick: ((Song) -> Unit)? = null,
    private val showSavedIcon: Boolean = false
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition: Int = -1
    private val handler = Handler(Looper.getMainLooper())

    // Limpia el estado de la canción anterior
    private fun clearPreviousSongState() {
        mediaPlayer?.release()
        mediaPlayer = null
        if (currentPlayingPosition != -1) {
            notifyItemChanged(currentPlayingPosition) // Refresca el ícono y barra de progreso de la canción anterior
            currentPlayingPosition = -1
        }
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
    }

    // Actualiza la lista de canciones
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    // Actualiza el estado de favorito de una canción específica
    fun updateFavoriteStatus(songId: Long, isFavorite: Boolean) {
        val songIndex = songs.indexOfFirst { it.id == songId }
        if (songIndex != -1) {
            val updatedSong = songs[songIndex].copy(isFavorite = isFavorite)
            songs = songs.toMutableList().apply {
                set(songIndex, updatedSong)
            }
            notifyItemChanged(songIndex)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding, parent.context)
    }

    override fun getItemCount(): Int = songs.size

    inner class SongViewHolder(private val binding: ItemSongBinding, private val context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        private val updateProgress = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    binding.progressBar.progress = it.currentPosition / 1000
                    handler.postDelayed(this, 1000)
                }
            }
        }

        fun bind(song: Song) {
            binding.songTitleTextView.text = song.title
            binding.artistNameTextView.text = song.artist
            binding.durationTextView.text = context.getString(R.string.song_duration, song.duration)
            Picasso.get().load(song.coverUrl).into(binding.coverImageView)

            // Configura el ícono de favorito
            if (showSavedIcon || song.isFavorite) {
                binding.favoriteImageView.setImageResource(R.drawable.ic_star_saved)
                binding.favoriteImageView.setOnClickListener {
                    onRemoveFavoriteClick?.invoke(song)
                }
            } else {
                binding.favoriteImageView.setImageResource(R.drawable.ic_star)
                binding.favoriteImageView.setOnClickListener {
                    onFavoriteClick(song)
                }
            }

            // Actualiza el ícono y barra de progreso según la canción en reproducción
            if (bindingAdapterPosition == currentPlayingPosition) {
                binding.playPauseImageView.setImageResource(R.drawable.ic_pause)
                handler.post(updateProgress) // Inicia la actualización de progreso
            } else {
                binding.playPauseImageView.setImageResource(R.drawable.ic_play)
                binding.progressBar.progress = 0
                handler.removeCallbacks(updateProgress) // Detiene la actualización de progreso
            }

            binding.playPauseImageView.setOnClickListener {
                if (bindingAdapterPosition == currentPlayingPosition) {
                    // Toggle play/pausa en la canción actual
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.pause()
                        binding.playPauseImageView.setImageResource(R.drawable.ic_play)
                    } else {
                        mediaPlayer?.start()
                        binding.playPauseImageView.setImageResource(R.drawable.ic_pause)
                        handler.post(updateProgress)
                    }
                } else {
                    // Limpia el estado de la canción anterior
                    clearPreviousSongState()

                    // Configura y reproduce la nueva canción
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(song.previewUrl)
                        setOnPreparedListener {
                            start()
                            binding.playPauseImageView.setImageResource(R.drawable.ic_pause)
                            binding.progressBar.max = duration / 1000
                            binding.progressBar.progress = 0
                            handler.post(updateProgress)
                        }
                        setOnCompletionListener {
                            binding.playPauseImageView.setImageResource(R.drawable.ic_play)
                            binding.progressBar.progress = 0
                            handler.removeCallbacks(updateProgress)
                        }
                        prepareAsync()
                    }
                    currentPlayingPosition = bindingAdapterPosition
                }
            }
        }
    }
}