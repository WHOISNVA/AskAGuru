package com.example.askguru.ui.qr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askguru.databinding.ItemRecommendationBinding
import com.example.askguru.ui.recommendation.RecommendationAdapter
import com.example.askguru.ui.recommendation.RecommendationListener
import com.example.askguru.viewmodel.home.Recommendation

class RecommendationDataAdapter (private val playList: ArrayList<RecommendationData>): RecyclerView.Adapter<RecommendationDataAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemRecommendationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )


    override fun getItemCount(): Int = playList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(playList[position],position)
    }


    inner class ViewHolder(private val binding: ItemRecommendationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(list: RecommendationData, position: Int) {

            binding.ivAccepted.visibility = View.GONE
            binding.ivRejected.visibility = View.GONE

            binding.tvTitle.text =  list.song_title
            binding.tvSongBy.text = "By: ${list.username}"
            Glide.with(binding.ivImage.context)
                .load(list.artwork)
                .into(binding.ivImage)

//            binding.ivAccepted.setOnClickListener {
//                listener.onClickAccepted(list,position)
//            }
//
//            binding.ivRejected.setOnClickListener {
//                listener.onClickRejected(list,position)
//            }


        }
    }


}