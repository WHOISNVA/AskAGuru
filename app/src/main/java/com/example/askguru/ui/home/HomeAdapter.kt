package com.example.askguru.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.ItemHomeBinding

import com.example.askguru.viewmodel.home.PlayListResponseItem

class HomeAdapter(private val playList: ArrayList<PlayListResponseItem>, val listener: SongSelectedClickListener):
    RecyclerView.Adapter<HomeAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )


    override fun getItemCount(): Int = playList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(playList[position])
    }


    inner class ViewHolder(private val binding: ItemHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(list: PlayListResponseItem) {


            binding.tvTitle.text =  list.playlist.songTitle
            binding.tvSongBy.text = "By: ${list.playlist.username}"

            list.playlist.playlistLikeCount.let {
                binding.tvLikeCount.text = "${list.playlist.playlistLikeCount}"
            }

            list.playlist.listens.let {
                binding.tvPlayCount.text = "${list.playlist.listens}"
            }



            Glide.with(binding.ivImage.context)
                .load(list.playlist.artwork)
                .into(binding.ivImage)

            binding.ivImage.setOnClickListener {
                //listener.onPlaylistClick(list)
                listener.onPlayClick(list)
            }

            binding.ivPlay.setOnClickListener {
                listener.onPlayClick(list)
            }
//            itemView.setOnClickListener {
//                val builder = CustomTabsIntent.Builder()
//                val customTabsIntent = builder.build()
//                customTabsIntent.launchUrl(it.context, Uri.parse(article.url))
//            }

            binding.llRow.setOnClickListener {
                listener.onPlaylistClick(list)
            }
        }
    }

    fun addData(list: List<PlayListResponseItem>) {
        playList.clear()
        notifyDataSetChanged()
        playList.addAll(list)
        notifyDataSetChanged()
    }

}