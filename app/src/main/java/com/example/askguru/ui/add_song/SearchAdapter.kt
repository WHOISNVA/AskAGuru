package com.example.askguru.ui.add_song

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.ItemSearchBinding
import com.example.askguru.viewmodel.add_song.SearchList

class SearchAdapter(private val searchList: ArrayList<SearchList>, private val listener: SearchSelectClickListener): RecyclerView.Adapter<SearchAdapter.ViewHolder>()   {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SearchAdapter.ViewHolder(
        ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false),listener
    )

    override fun getItemCount(): Int {
        return  searchList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(searchList[position],position)
    }

    fun addList(list: ArrayList<SearchList>) {
        searchList.clear()
        searchList.addAll(list)
        notifyDataSetChanged()

    }

    fun getList(): ArrayList<SearchList> {
        return searchList
    }


    class ViewHolder(private val binding: ItemSearchBinding,
                     private val listener : SearchSelectClickListener) : RecyclerView.ViewHolder(binding.root) {

        fun bind(list: SearchList, position: Int) {
            //binding.tvSongTitle.text =  list.attributes.albumName
            binding.tvSongTitle.text =  list.attributes.name
            binding.tvSubTitle.text =list.attributes.artistName

            list.attributes.artwork.url.let {
                Glide.with(binding.ivImage.context)
                    .load(list.attributes.artwork.url)

                    .into(binding.ivImage)
            }


            if(list.isSelected){
                binding.ivRadio.setImageDrawable(binding.ivRadio.context.getDrawable(R.drawable.ic_check))
            }else{
                binding.ivRadio.setImageDrawable(binding.ivRadio.context.getDrawable(R.drawable.ic_uncheck))
            }

            binding.ivRadio.setOnClickListener {

                listener.onClickSong(position,list)



            }

        }
    }
}