package com.example.askguru.ui.recommendation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askguru.databinding.ItemRecommendationBinding

import com.example.askguru.viewmodel.home.Recommendation


class RecommendationAdapter (
    private val playList: ArrayList<Recommendation>,
    val listener: RecommendationListener): RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemRecommendationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )


    override fun getItemCount(): Int = playList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(playList[position],position)
    }


    inner class ViewHolder(private val binding: ItemRecommendationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(list: Recommendation, position: Int) {
            binding.tvTitle.text =  list.songTitle
            binding.tvSongBy.text = "By: ${list.username}"
            Glide.with(binding.ivImage.context)
                .load(list.artwork)
                .into(binding.ivImage)

            binding.ivAccepted.setOnClickListener {
                listener.onClickAccepted(list,position)
            }

            binding.ivRejected.setOnClickListener {
                listener.onClickRejected(list,position)
            }

            itemView.setOnClickListener {
                listener.onItemClick(list,position)
            }
        }
    }


}