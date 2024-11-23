package com.alecocluc.musify.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alecocluc.musify.R
import com.alecocluc.musify.databinding.ItemAlbumBinding
import com.alecocluc.musify.models.Album
import com.squareup.picasso.Picasso

class AlbumAdapter(
    private var albums: List<Album>
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]
        holder.bind(album)
    }

    override fun getItemCount(): Int = albums.size

    // Actualiza la lista de álbumes
    fun updateAlbums(newAlbums: List<Album>) {
        albums = newAlbums
        notifyDataSetChanged()
    }

    inner class AlbumViewHolder(private val binding: ItemAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            binding.albumTitleTextView.text = album.title
            binding.artistNameTextView.text = album.artist
            Picasso.get().load(album.coverUrl).error(R.drawable.square_placeholder).into(binding.albumImageView)
        }
    }
}
