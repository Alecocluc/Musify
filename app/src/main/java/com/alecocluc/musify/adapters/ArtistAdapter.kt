package com.alecocluc.musify.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alecocluc.musify.R
import com.alecocluc.musify.databinding.ItemArtistBinding
import com.alecocluc.musify.models.Artist
import com.squareup.picasso.Picasso

class ArtistAdapter(
    private var artists: List<Artist>
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemArtistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]
        holder.bind(artist)
    }

    override fun getItemCount(): Int = artists.size

    // Actualiza la lista de artistas
    fun updateArtists(newArtists: List<Artist>) {
        artists = newArtists
        notifyDataSetChanged()
    }

    inner class ArtistViewHolder(private val binding: ItemArtistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            binding.artistNameTextView.text = artist.name
            Picasso.get().load(artist.pictureUrl).error(R.drawable.square_placeholder).into(binding.artistImageView)
        }
    }
}
