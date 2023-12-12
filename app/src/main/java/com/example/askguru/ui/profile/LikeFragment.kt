package com.example.askguru.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.askguru.R
import com.example.askguru.databinding.FragmentLikeBinding
import com.example.askguru.ui.home.GenreAdapter
import com.example.askguru.ui.home.GenreClickListener
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.viewmodel.home.GenreModel
import com.example.askguru.viewmodel.profile.LikedPlaylists
import com.example.askguru.viewmodel.profile.MyPlaylists
import com.example.askguru.viewmodel.profile.Playlists
import com.google.gson.Gson


class LikeFragment : Fragment(), GenreClickListener {

    private var likeList: ArrayList<LikedPlaylists> = arrayListOf()
    val tempList: ArrayList<LikedPlaylists> = ArrayList()
    val searchList: ArrayList<LikedPlaylists> = ArrayList()

    private var _binding: FragmentLikeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LikeAdapter

    private val genreList = ArrayList<GenreModel>()
    private lateinit var genreAdapter: GenreAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLikeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistString =  PreferenceHelper.getStringPreference(requireContext(),"playlists")
        val playListModel = Gson().fromJson(playlistString, Playlists::class.java)
        likeList = playListModel.liked_playlists as ArrayList<LikedPlaylists>
        tempList.addAll(likeList)
        searchList.addAll(likeList)

        setListAdapter()

        setGenreListData()

        /*** Filter Module ***/
        binding.tvFilter.setOnClickListener {
            binding.rvGenre.visibility = View.VISIBLE
            binding.llSort.visibility = View.GONE
            binding.llSearch.visibility = View.GONE

            binding.tvFilter.background =  ResourcesCompat.getDrawable(resources, R.drawable.orange_bg, context?.theme)
            binding.tvSort.background =   ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvSearch.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
        }

        binding.tvSort.setOnClickListener {
            binding.rvGenre.visibility = View.GONE
            binding.llSort.visibility = View.VISIBLE
            binding.llSearch.visibility = View.GONE

            binding.tvFilter.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvSort.background =   ResourcesCompat.getDrawable(resources, R.drawable.orange_bg, context?.theme)
            binding.tvSearch.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
        }

        binding.tvSearch.setOnClickListener {
            binding.rvGenre.visibility = View.GONE
            binding.llSort.visibility = View.GONE
            binding.llSearch.visibility = View.VISIBLE

            binding.tvFilter.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvSort.background =   ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvSearch.background =  ResourcesCompat.getDrawable(resources, R.drawable.orange_bg, context?.theme)
        }

        /*** Sort Module Click ****/
        binding.tvMostRecent.setOnClickListener {

            if(likeList.isEmpty())
                return@setOnClickListener

            binding.tvMostRecent.background =  ResourcesCompat.getDrawable(resources, R.drawable.orange_bg, context?.theme)
            binding.tvLongest.background =   ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvMostListens.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)

            val sortList: ArrayList<LikedPlaylists> = ArrayList()
            sortList.addAll(tempList)
            adapter.setListData(sortList)
        }

        binding.tvLongest.setOnClickListener {

            if(likeList.isEmpty())
                return@setOnClickListener

            binding.tvMostRecent.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvLongest.background =   ResourcesCompat.getDrawable(resources, R.drawable.orange_bg, context?.theme)
            binding.tvMostListens.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)

            val sortList: ArrayList<LikedPlaylists> = ArrayList()
            sortList.addAll(tempList)
            sortList.sortBy { it.playlist.songDuration }
            sortList.reverse()

            adapter.setListData(sortList)
        }

        binding.tvMostListens.setOnClickListener {

            if(likeList.isEmpty())
                return@setOnClickListener

            binding.tvMostRecent.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvLongest.background =   ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvMostListens.background =  ResourcesCompat.getDrawable(resources, R.drawable.orange_bg, context?.theme)

            val sortList: ArrayList<LikedPlaylists> = ArrayList()
            sortList.addAll(tempList)
            sortList.sortBy { it.playlist.listens }
            sortList.reverse()

            adapter.setListData(sortList)
        }


        /*** Search Module ****/
        binding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(searchText: Editable?) {

                if(likeList.isEmpty())
                    return

                if(binding.edSearch.text.toString().trim().length>2)
                    filter(binding.edSearch.text.toString().trim())
                else {
                    adapter.setListData(searchList)
                    binding.tvNoData.visibility = View.GONE
                    binding.rvLikeList.visibility = View.VISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

    }

    private fun setListAdapter() {
        adapter = LikeAdapter(likeList)
        _binding?.rvLikeList!!.layoutManager = LinearLayoutManager(requireActivity())
        _binding?.rvLikeList!!.addItemDecoration(DividerItemDecoration(_binding?.rvLikeList!!.context,
            (_binding?.rvLikeList!!.layoutManager as LinearLayoutManager).orientation))
        _binding?.rvLikeList!!.adapter = adapter

    }


    /*** Genre Module ***/
    private fun setGenreListData() {
        genreList.clear()
        genreList.add(GenreModel("All", true))
        genreList.add(GenreModel("Afrobeat", false))
        genreList.add(GenreModel("Blues", false))
        genreList.add(GenreModel("Classical", false))
        genreList.add(GenreModel("Country", false))
        genreList.add(GenreModel("Electronic", false))
        genreList.add(GenreModel("Funk", false))
        genreList.add(GenreModel("Hip-Hop", false))
        genreList.add(GenreModel("Instrumental", false))
        genreList.add(GenreModel("Jazz", false))
        genreList.add(GenreModel("Kpop", false))
        genreList.add(GenreModel("Latin", false))
        genreList.add(GenreModel("Metal", false))
        genreList.add(GenreModel("Pop", false))
        genreList.add(GenreModel("Punk", false))
        genreList.add(GenreModel("Reggae", false))
        genreList.add(GenreModel("Rock", false))
        genreList.add(GenreModel("Soul", false))
        genreList.add(GenreModel("funk", false))
        genreList.add(GenreModel("gospel", false))
        genreList.add(GenreModel("pop", false))

        genreAdapter = GenreAdapter(genreList, this)
        binding.rvGenre!!.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvGenre!!.adapter = genreAdapter

    }

    override fun onGenreClicked(genre: String, position: Int) {
        genreList.forEach { it.isSelected = false }
        genreList[position].isSelected = true
        genreAdapter.notifyDataSetChanged()
        Log.e("kp", "selected genre ===> $genre")
        getListByGenre(genre)
    }

    private fun getListByGenre(genre: String) {
        val filteredlist: ArrayList<LikedPlaylists> = ArrayList()
        if(genre == "All"){
            adapter.setListData(tempList)
            binding.tvNoData.visibility = View.GONE
            binding.rvLikeList.visibility = View.VISIBLE
        }else{
            if (tempList.isNotEmpty()) {
                tempList.forEach {
                    if (it.playlist.genre.toLowerCase().contains(genre.toLowerCase())) {
                        filteredlist.add(it)
                    }
                }
            }
            if (filteredlist.isNotEmpty()) {
                adapter.setListData(filteredlist)
                binding.tvNoData.visibility = View.GONE
                binding.rvLikeList.visibility = View.VISIBLE
            }else{
                binding.tvNoData.visibility = View.VISIBLE
                binding.rvLikeList.visibility = View.GONE
            }
        }
    }

    /**** Search module ****/
    private fun filter(text: String) {
        val filteredlist: ArrayList<LikedPlaylists> = ArrayList()
        for (item in likeList) {
            if (item.playlist.songTitle.toLowerCase().contains(text.toLowerCase())) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
            binding.rvLikeList.visibility = View.GONE
        } else {
            Log.e("kp","Filter list size == ${filteredlist.size}")
            adapter.setListData(filteredlist)
            binding.tvNoData.visibility = View.GONE
            binding.rvLikeList.visibility = View.VISIBLE
        }
    }


}