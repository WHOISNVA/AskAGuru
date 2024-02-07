package com.example.askguru.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.askguru.databinding.ItemHomeGridBinding
import com.example.askguru.viewmodel.home.PlayListResponseItem

class HomeAdapterGrid (private val playList: ArrayList<PlayListResponseItem>,
private val listener : SongSelectedClickListener): RecyclerView.Adapter<HomeAdapterGrid.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemHomeGridBinding.inflate(LayoutInflater.from(parent.context), parent, false),listener
    )


    override fun getItemCount(): Int = playList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(playList[position],position)
    }


    class ViewHolder(private val binding: ItemHomeGridBinding,private val listener : SongSelectedClickListener) :

        RecyclerView.ViewHolder(binding.root) {
        fun bind(list: PlayListResponseItem, position: Int) {


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

            binding.ivLike.setOnClickListener {
                listener.onLikeClicked(list.playlist.playlistId,list,position)

            }

            binding.ivImage.setOnClickListener {
                listener.onPlaylistClick(list)
            }

            binding.ivPlay.setOnClickListener {
                listener.onPlayClick(list)
            }


        }
    }

    fun addData(list: List<PlayListResponseItem>) {

        playList.addAll(list)
        notifyDataSetChanged()
    }

    fun updateList(position: Int, count: Int) {
        playList[position].playlist.playlistLikeCount = count
        notifyItemChanged(position)
        notifyDataSetChanged()

    }

}