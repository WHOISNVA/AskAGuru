package com.example.askguru.ui.profile

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askguru.databinding.ItemContributionBinding
import com.example.askguru.viewmodel.profile.Contribution

class ContributionsAdapter(
    val activity: Activity,
    private val contributions: ArrayList<Contribution>
) : RecyclerView.Adapter<ContributionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemContributionBinding.inflate(
                LayoutInflater.from(activity),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = contributions[position].playlist
        val binding = holder.binding

        Glide.with(binding.imageView5).load(playlist.artwork).into(binding.imageView5)
        binding.txtTrackName.text = playlist.songTitle
    }

    override fun getItemCount(): Int {
        return contributions.size
    }

    fun setListData(tempList: java.util.ArrayList<Contribution>) {
        contributions.clear()
        contributions.addAll(tempList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemContributionBinding) :
        RecyclerView.ViewHolder(binding.root)
}