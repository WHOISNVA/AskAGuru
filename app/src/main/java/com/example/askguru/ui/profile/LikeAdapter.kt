package com.example.askguru.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.ItemLikesBinding
import com.example.askguru.databinding.ItemRankingLayoutBinding
import com.example.askguru.ui.ranking.RankAdapter
import com.example.askguru.utils.Const
import com.example.askguru.viewmodel.profile.LikedPlaylists
import com.example.askguru.viewmodel.ranking.RankListResponseItem

class LikeAdapter( private val likeList: ArrayList<LikedPlaylists>): RecyclerView.Adapter<LikeAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LikeAdapter.ViewHolder(
        ItemLikesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount(): Int {
        return  likeList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(likeList[position],position)
    }

    fun setListData(tempList:ArrayList<LikedPlaylists>) {
        likeList.clear()
        likeList.addAll(tempList)
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemLikesBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(list: LikedPlaylists, position: Int) {
            binding.tvName.text =  list.playlist.songTitle
            list.playlist.artwork.let {
                Glide.with(binding.ivImage.context)
                    .load(list.playlist.artwork)
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.ivImage)
            }

        }
    }
}