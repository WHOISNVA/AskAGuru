package com.example.askguru.ui.ranking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.ItemRankingLayoutBinding
import com.example.askguru.utils.Const
import com.example.askguru.viewmodel.ranking.RankListResponse
import com.example.askguru.viewmodel.ranking.RankListResponseItem

class RankAdapter( private val rankList: ArrayList<RankListResponseItem>, private val listener: RankProfileClickListener): RecyclerView.Adapter<RankAdapter.ViewHolder>()  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemRankingLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )



    override fun getItemCount(): Int {
       return  rankList.size
    }

    override fun onBindViewHolder(holder: RankAdapter.ViewHolder, position: Int) {
        holder.bind(rankList[position],position)
    }

    fun addList(list: RankListResponse) {
        rankList.addAll(list)
        notifyDataSetChanged()
    }

    fun cleatList() {
        rankList.clear()
        notifyDataSetChanged()

    }


    inner  class ViewHolder(private val binding: ItemRankingLayoutBinding) : RecyclerView.ViewHolder(binding.root)  {
        fun bind(list: RankListResponseItem, position: Int) {
            binding.tvUserName.text =  list.username

            binding.tvCount.text = (position+4).toString()

            list.total_listens.let {
                binding.tvTotalListens.text = list.total_listens.toString()
            }
            list.profile_pic.let {
                Glide.with(binding.ivImage.context)
                    .load(Const.IMAGE_BASE_URL+list.profile_pic)
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.ivImage)
            }

            binding.llRow.setOnClickListener {
                listener.onRankProfileClick(list.user_id)
            }

        }
    }

}