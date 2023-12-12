package com.example.askguru.ui.ranking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.ItemRankingLayoutBinding
import com.example.askguru.utils.Const
import com.example.askguru.viewmodel.ranking.RankingByRecommendedItem
import com.example.askguru.viewmodel.ranking.RankingByRecommendedResponse

class RankAdapterRecommended ( private val rankList: ArrayList<RankingByRecommendedItem>, private val listener: RankProfileClickListener): RecyclerView.Adapter<RankAdapterRecommended.ViewHolder>()  {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemRankingLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )



    override fun getItemCount(): Int {
        return  rankList.size
    }

    override fun onBindViewHolder(holder: RankAdapterRecommended.ViewHolder, position: Int) {
        holder.bind(rankList[position],position)
    }

    fun addList(list: RankingByRecommendedResponse) {

        rankList.addAll(list)
        notifyDataSetChanged()
    }


    inner class ViewHolder(private val binding: ItemRankingLayoutBinding) : RecyclerView.ViewHolder(binding.root)  {

        fun bind(list: RankingByRecommendedItem, position: Int) {

            binding.tvUserName.text =  list.user.username

            binding.tvCount.text = (position+1).toString()

            list.accepted_recs_count.let {
                binding.tvTotalListens.text = list.accepted_recs_count.toString()
            }
            list.user.profile_pic.let {
                Glide.with(binding.ivImage.context)
                    .load(Const.IMAGE_BASE_URL+list.user.profile_pic)
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.ivImage)
            }

            binding.llRow.setOnClickListener {
                listener.onRankProfileClick(list.user.user_id)
            }

        }
    }

}