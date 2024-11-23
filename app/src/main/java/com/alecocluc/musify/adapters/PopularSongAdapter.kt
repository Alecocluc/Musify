package com.alecocluc.musify.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alecocluc.musify.R
import com.alecocluc.musify.databinding.ItemPopularSongBinding
import com.alecocluc.musify.models.Song
import com.squareup.picasso.Picasso

class PopularSongAdapter(
    private var songs: List<Song>,
    private val onItemClick: (Song) -> Unit
) : RecyclerView.Adapter<PopularSongAdapter.PopularSongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularSongViewHolder {
        val binding = ItemPopularSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PopularSongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopularSongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int = songs.size

    // Actualiza la lista de canciones
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    inner class PopularSongViewHolder(private val binding: ItemPopularSongBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.songTitleTextView.text = song.title
            binding.songArtistTextView.text = song.artist
            Picasso.get().load(song.coverUrl).error(R.drawable.square_placeholder).into(binding.songCoverImageView)

            // Configura el clic en el elemento
            binding.root.setOnClickListener {
                onItemClick(song)
            }
        }
    }
}
