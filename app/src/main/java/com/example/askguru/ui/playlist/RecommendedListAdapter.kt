package com.example.askguru.ui.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.ListItemTrackBinding
import com.example.askguru.utils.Const
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.viewmodel.home.Recommendation

class RecommendedListAdapter(val mainActivity: FragmentActivity,
                             val recommendations: List<Recommendation>?,
                             val listener :AddSongClickListener) : RecyclerView.Adapter<RecommendedListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemTrackBinding.inflate(LayoutInflater.from(mainActivity)))
    }

    override fun getItemCount(): Int {
        return recommendations?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = recommendations?.get(position)
        holder.bind(model, position)
    }

    inner class ViewHolder(val binding: ListItemTrackBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Recommendation?, position: Int) {
            binding.txtSrNumber.text = "${position + 1}"

            binding.tvSongName.text = model?.songTitle
            binding.txtArtistName.text = model?.artistName

            model?.artwork.let {
                Glide.with(binding.ivSongThumb.context)
                    .load(model?.artwork)
                    .placeholder(R.drawable.img2)
                    .into(binding.ivSongThumb)
            }

            binding.ivAdd.setOnClickListener {
                if(PreferenceHelper.getBooleanPreference(mainActivity,Const.PRE_IS_LOGIN,false)) {
                    listener.onAddSongClick(model,position)
                }else{
                    Toast.makeText(mainActivity,"Please login first",Toast.LENGTH_SHORT).show()
                }
            }

            itemView.setOnClickListener {
                listener.onItemClick(model,position)
            }
        }

    }

}