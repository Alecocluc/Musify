package com.alecocluc.musify.adapters

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alecocluc.musify.R
import com.alecocluc.musify.databinding.ItemSongSearchBinding
import com.alecocluc.musify.models.Song
import com.squareup.picasso.Picasso

class SongAdapterSearch(
    var songs: List<Song>,
    private val onFavoriteClick: (Song) -> Unit,
    private val onRemoveFavoriteClick: ((Song) -> Unit)? = null
) : RecyclerView.Adapter<SongAdapterSearch.SongViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition: Int = -1
    private val handler = Handler(Looper.getMainLooper())

    private fun clearPreviousSongState() {
        mediaPlayer?.release()
        mediaPlayer = null
        if (currentPlayingPosition != -1) {
            notifyItemChanged(currentPlayingPosition)
            currentPlayingPosition = -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int = songs.size

    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

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

    inner class SongViewHolder(
        private val binding: ItemSongSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val updateProgress = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    handler.postDelayed(this, 1000)
                }
            }
        }

        fun bind(song: Song) {
            binding.songTitleTextView.text = song.title
            binding.artistNameTextView.text = song.artist
            Picasso.get().load(song.coverUrl).into(binding.coverImageView)

            // Configura el ícono de favorito basado en el estado de isFavorite
            if (song.isFavorite) {
                binding.favoriteImageView.setImageResource(R.drawable.ic_star_saved)
                binding.favoriteImageView.setOnClickListener { onRemoveFavoriteClick?.invoke(song) }
            } else {
                binding.favoriteImageView.setImageResource(R.drawable.ic_star)
                binding.favoriteImageView.setOnClickListener { onFavoriteClick(song) }
            }

            if (bindingAdapterPosition == currentPlayingPosition) {
                binding.playPauseImageView.setImageResource(R.drawable.ic_pause)
                handler.post(updateProgress)
            } else {
                binding.playPauseImageView.setImageResource(R.drawable.ic_play)
                handler.removeCallbacks(updateProgress)
            }

            binding.playPauseImageView.setOnClickListener {
                if (bindingAdapterPosition == currentPlayingPosition) {
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.pause()
                        binding.playPauseImageView.setImageResource(R.drawable.ic_play)
                    } else {
                        mediaPlayer?.start()
                        binding.playPauseImageView.setImageResource(R.drawable.ic_pause)
                        handler.post(updateProgress)
                    }
                } else {
                    clearPreviousSongState()
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(song.previewUrl)
                        setOnPreparedListener {
                            start()
                            binding.playPauseImageView.setImageResource(R.drawable.ic_pause)
                            handler.post(updateProgress)
                        }
                        setOnCompletionListener {
                            binding.playPauseImageView.setImageResource(R.drawable.ic_play)
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
