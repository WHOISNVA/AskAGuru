package com.example.askguru.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.askguru.R
import com.example.askguru.databinding.ItemGenreBinding
import com.example.askguru.viewmodel.home.GenreModel

class GenreAdapter(

    private val genreList: ArrayList<GenreModel>,
    private val listener: GenreClickListener
): RecyclerView.Adapter<GenreAdapter.ViewHolder>()  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount(): Int {
        return  genreList.size
    }

    override fun onBindViewHolder(holder: GenreAdapter.ViewHolder, position: Int) {
        holder.bind(genreList[position],position,listener)
    }

    class ViewHolder(private val binding: ItemGenreBinding) : RecyclerView.ViewHolder(binding.root)  {
        fun bind(genreModel: GenreModel, position: Int, listener: GenreClickListener) {
            binding.tvGenre.text = genreModel.genre

            if(genreModel.isSelected){
                binding.tvGenre.background = (binding.tvGenre.context.resources.getDrawable(R.drawable.orange_bg))
            }else{
                binding.tvGenre.background = (binding.tvGenre.context.resources.getDrawable(R.drawable.music_type_bg))
            }

            binding.tvGenre.setOnClickListener {
                listener.onGenreClicked(genreModel.genre,position)
            }
        }
    }

}