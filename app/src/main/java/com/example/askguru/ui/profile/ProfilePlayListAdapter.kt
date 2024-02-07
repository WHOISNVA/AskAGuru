package com.example.askguru.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.ItemProfilePlaylistBinding
import com.example.askguru.viewmodel.profile.MyPlaylists
import com.example.askguru.viewmodel.profile.Playlist
import java.util.ArrayList

class ProfilePlayListAdapter(val requireActivity: FragmentActivity,
                             private val myPlayList: ArrayList<MyPlaylists>,
                             private val listener: ProfilePlayListClickListener) : RecyclerView.Adapter<ProfilePlayListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemProfilePlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = myPlayList[position].playlist
        holder.bind(playlist,position)
    }

    override fun getItemCount(): Int {
        return myPlayList.size
    }

    fun setListData(filteredlist: ArrayList<MyPlaylists>) {
        myPlayList.clear()
        myPlayList.addAll(filteredlist)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemProfilePlaylistBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(list: Playlist, position: Int) {

            binding.tvName.text =  list.songTitle

            list.artwork.let {
                Glide.with(binding.ivImage.context)
                    .load(list.artwork)
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.ivImage)
            }

            binding.ivRemove.setOnClickListener {
               // listener.onDeleteClicked(list,position, myPlayList[position])
            }

            binding.llRowList.setOnClickListener {
                listener.onPlayListClicked(list,position)
            }

            binding.ivShare.setOnClickListener {
                listener.onShareClicked(list,position)
            }

        }
    }


}
