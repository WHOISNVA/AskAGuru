package com.example.askguru.ui.ranking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.ItemRankingLayoutBinding
import com.example.askguru.utils.Const
import com.example.askguru.viewmodel.ranking.RankingByFollowerCountResponse
import com.example.askguru.viewmodel.ranking.RankingByFollowerCountResponseItem

class RankAdapterFollower (private val rankList: ArrayList<RankingByFollowerCountResponseItem>): RecyclerView.Adapter<RankAdapterFollower.ViewHolder>()  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemRankingLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )



    override fun getItemCount(): Int {
        return  rankList.size
    }

    override fun onBindViewHolder(holder: RankAdapterFollower.ViewHolder, position: Int) {
        holder.bind(rankList[position],position)
    }

    fun addList(list: RankingByFollowerCountResponse) {
        rankList.addAll(list)
        notifyDataSetChanged()
    }


    class ViewHolder(private val binding: ItemRankingLayoutBinding) : RecyclerView.ViewHolder(binding.root)  {
        fun bind(list: RankingByFollowerCountResponseItem, position: Int) {
            binding.tvUserName.text =  list.username

            binding.tvCount.text = (position+1).toString()

            list.follower_data.follower_count.let {
                binding.tvTotalListens.text = list.follower_data.follower_count.toString()
            }

            list.profile_pic.let {
                Glide.with(binding.ivImage.context)
                    .load(Const.IMAGE_BASE_URL+list.profile_pic)
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.ivImage)
            }

        }
    }

}