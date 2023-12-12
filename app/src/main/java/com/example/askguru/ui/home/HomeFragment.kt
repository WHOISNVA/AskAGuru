package com.example.askguru.ui.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.askguru.R
import com.example.askguru.databinding.FragmentHomeBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.utils.Const
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.utils.SpotifyHelper
import com.example.askguru.viewmodel.home.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson


class HomeFragment : Fragment(), SongSelectedClickListener, GenreClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeVm

    private lateinit var adapter: HomeAdapter
    private lateinit var adapterGrid: HomeAdapterGrid
    private lateinit var adapterGridMost: HomeAdapterGrid
    private lateinit var adapterGridLong: HomeAdapterGrid

    val songList: ArrayList<PlayListResponseItem> = ArrayList()
    val mostPlayList: ArrayList<PlayListResponseItem> = ArrayList()
    val longestPlayList: ArrayList<PlayListResponseItem> = ArrayList()

    val tempList: ArrayList<PlayListResponseItem> = ArrayList()



    private val genreList = ArrayList<GenreModel>()
    private lateinit var genreAdapter: GenreAdapter




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("kp", "Token -- ${PreferenceHelper.getStringPreference(requireContext(), Const.PRE_AUTHORIZATION_TOKEN)}")

        if (PreferenceHelper.getBooleanPreference(requireContext(), Const.PRE_IS_LOGIN, false)) {
            binding.nestedScroll.visibility = View.VISIBLE
            binding.llNormalList.visibility = View.GONE
            setGridAdapter()
        } else {
            binding.nestedScroll.visibility = View.GONE
            binding.llNormalList.visibility = View.VISIBLE
            //setNormalAdapter()
        }

        setGenreListData()

        viewModel = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[HomeVm::class.java]

        getPlayListApiCall()

        _binding?.edSearch!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(searchText: Editable?) {
                if (searchText!!.isNotEmpty())
                    filter(_binding?.edSearch!!.text.toString().trim())
                else {
                    // adapter.addData(songList)
                    setNormalAdapter()

                    setGenreListData()
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })



         //getInstallerPackageName(requireActivity(),"com.apple.android.music")


    }

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
        _binding?.rvGenre!!.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        _binding?.rvGenre!!.adapter = genreAdapter

    }

    override fun onGenreClicked(genre: String, position: Int) {
        genreList.forEach { it.isSelected = false }
        genreList[position].isSelected = true
        genreAdapter.notifyDataSetChanged()
        Log.e("kp", "selected genre ===> $genre")

        getListByGenre(genre)
    }

    private fun getListByGenre(genre: String) {

        if (PreferenceHelper.getBooleanPreference(requireContext(), Const.PRE_IS_LOGIN, false)) {
            val filteredlist: ArrayList<PlayListResponseItem> = ArrayList()

            if(genre == "All"){
                setGridAdapter()
            }else{
                if (tempList.isNotEmpty()) {
                    tempList.forEach {
                        if (it.playlist.genre.toLowerCase().contains(genre.toLowerCase())) {
                            filteredlist.add(it)
                        }
                    }
                }

                if (filteredlist.isNotEmpty()) {

                    adapterGrid = HomeAdapterGrid(filteredlist, this)
                    _binding?.rvRecentPlayList!!.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
                    _binding?.rvRecentPlayList!!.adapter = adapterGrid


                    //filteredlist.sortByDescending { it.playlist.listens }
                    adapterGridMost = HomeAdapterGrid(filteredlist, this)
                    _binding?.rvMostPlayList!!.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
                    _binding?.rvMostPlayList!!.adapter = adapterGridMost

                    //filteredlist.sortByDescending { it.playlist.songDuration }
                    adapterGridLong = HomeAdapterGrid(filteredlist, this)
                    _binding?.rvLongestPlayList!!.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
                    _binding?.rvLongestPlayList!!.adapter = adapterGridLong

                }else{
                    Toast.makeText(requireActivity(), "No data found of $genre", Toast.LENGTH_LONG).show()
                }

            }


        }else{
            val filteredlist: ArrayList<PlayListResponseItem> = ArrayList()
            if (tempList.isNotEmpty()) {
                tempList.forEach {
                    if (it.playlist.genre.toLowerCase().contains(genre.toLowerCase())) {
                        filteredlist.add(it)
                    }
                }
            }

            if (filteredlist.isNotEmpty()) {
                adapter.addData(filteredlist) // filterList(filteredlist)
            }
        }

    }

    /* Set Adapter */
    private fun setNormalAdapter() {
        songList.sortBy { it.playlist.songTitle }
        adapter = HomeAdapter(songList, this)
        _binding?.rvPlayList!!.layoutManager = LinearLayoutManager(requireActivity())
        _binding?.rvPlayList!!.addItemDecoration(
            DividerItemDecoration(_binding?.rvPlayList!!.context, (_binding?.rvPlayList!!.layoutManager as LinearLayoutManager).orientation)
        )
        _binding?.rvPlayList!!.adapter = adapter

    }

    private fun setGridAdapter() {
        adapterGrid = HomeAdapterGrid(songList, this)
        _binding?.rvRecentPlayList!!.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        _binding?.rvRecentPlayList!!.adapter = adapterGrid


        adapterGridMost = HomeAdapterGrid(mostPlayList, this)
        _binding?.rvMostPlayList!!.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        _binding?.rvMostPlayList!!.adapter = adapterGridMost

        adapterGridLong = HomeAdapterGrid(longestPlayList, this)
        _binding?.rvLongestPlayList!!.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        _binding?.rvLongestPlayList!!.adapter = adapterGridLong


    }

    /*** Api Call */
    private fun getPlayListApiCall() {
        viewModel.getAllPlayList().observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        _binding?.shimmer!!.startShimmer()
                        _binding?.shimmer!!.visibility = View.VISIBLE

                        binding.nestedScroll.visibility = View.GONE
                        binding.llNormalList.visibility = View.GONE
                    }
                    Status.SUCCESS -> {
                        _binding?.shimmer!!.visibility = View.GONE
                        if (it.data?.isNotEmpty() == true) {
                            setListData(it.data)
                        }

                        showHideLayout()
                    }
                    Status.ERROR -> {
                        _binding?.shimmer!!.stopShimmer()
                        _binding?.shimmer!!.visibility = View.GONE
                        showHideLayout()
                    }
                }
            }
        })
    }

    private fun showHideLayout() {
        if (PreferenceHelper.getBooleanPreference(requireContext(), Const.PRE_IS_LOGIN, false)) {
            binding.nestedScroll.visibility = View.VISIBLE
            binding.llNormalList.visibility = View.GONE
        } else {
            binding.nestedScroll.visibility = View.GONE
            binding.llNormalList.visibility = View.VISIBLE
        }
    }

    private fun setListData(list: PlayListResponse) {
        songList.clear()
        tempList.clear()
        mostPlayList.clear()
        longestPlayList.clear()

        songList.addAll(list)
        tempList.addAll(list)


        if (PreferenceHelper.getBooleanPreference(requireContext(), Const.PRE_IS_LOGIN, false)) {

            mostPlayList.addAll(songList)
            longestPlayList.addAll(songList)

            mostPlayList.sortByDescending { it.playlist.listens }
            longestPlayList.sortByDescending { it.playlist.songDuration }

            Log.e("kp", "mostPlayList size == ${mostPlayList[0].playlist.songTitle}")
            Log.e("kp", "longestPlayList size == ${longestPlayList[0].playlist.songTitle}")

            adapterGrid.addData(songList)
            adapterGridMost.addData(mostPlayList)
            adapterGridLong.addData(longestPlayList)



        } else {

            songList.sortBy { it.playlist.songTitle }

            adapter = HomeAdapter(songList, this)
            _binding?.rvPlayList!!.layoutManager = LinearLayoutManager(requireActivity())
            _binding?.rvPlayList!!.addItemDecoration(
                DividerItemDecoration(_binding?.rvPlayList!!.context, (_binding?.rvPlayList!!.layoutManager as LinearLayoutManager).orientation)
            )
            _binding?.rvPlayList!!.adapter = adapter
//           adapter.addData(songList)
        }


        val playListResponseData = Gson().toJson(list)
        PreferenceHelper.setStringPreference(requireContext(), "playListResponseData", playListResponseData)



    }


    private fun filter(text: String) {
        val filteredlist: ArrayList<PlayListResponseItem> = ArrayList()
        if (tempList.isNotEmpty()) {
            tempList.forEach {
                if (it.playlist.songTitle.toLowerCase().contains(text.toLowerCase()) ||
                    it.playlist.username.toLowerCase().contains(text.toLowerCase())
                ) {
                    filteredlist.add(it)
                }
            }
        }

        Log.e("kp", "filter list size == ${filteredlist.size}")
        if (filteredlist.isNotEmpty()) {
            adapter.addData(filteredlist) // filterList(filteredlist)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onLikeClicked(playlistId: String, list: PlayListResponseItem, position: Int) {
        val token = "Bearer " + PreferenceHelper.getStringPreference(requireContext(), Const.PRE_AUTHORIZATION_TOKEN)
        likeApiCall(token, playlistId, list, position)
    }

    override fun onPlayClick(list: PlayListResponseItem) {

        Log.e("kp","songUrl == ${list.playlist.songUrl}")

        if (isPackageInstalled("com.apple.android.music", requireActivity().packageManager)) {

            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(list.playlist.songUrl)
            startActivity(openURL)

        } else {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_VIEW
            sendIntent.data = Uri.parse("market://details?id=com.apple.android.music")
            if (sendIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(sendIntent)
            }
        }

    }

    override fun onPlaylistClick(model: PlayListResponseItem) {
        if (model.playlist.playlistName == null) {
            model.playlist.playlistName = ""
        }
        model.playlist.recommendations.forEach {
            if (it.artwork == null) {
                it.artwork = ""
            }
        }

        val navController = activity?.findNavController(R.id.nav_host_fragment_activity_main)

        val bundle = Bundle()
        bundle.putSerializable("playlistModel", model)
        findNavController().navigate(R.id.navigation_playlist_details,bundle)

    }




    private fun likeApiCall(token: String, playlistId: String, list: PlayListResponseItem, position: Int) {

        viewModel.like(token, playlistId).observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {

                    }
                    Status.SUCCESS -> {
                        it.data?.like_count.let {
                            var count = it
                            list.playlist.playlistLikeCount = count!!
                            songList[position].playlist.playlistLikeCount = count
                            adapterGrid.notifyDataSetChanged()
                            adapterGridMost.notifyDataSetChanged()
                            adapterGridLong.notifyDataSetChanged()
                        }

                        Toast.makeText(requireContext(), "Liked", Toast.LENGTH_SHORT).show()
                    }
                    Status.ERROR -> {

                    }
                }
            }
        })
    }

    private fun showBottomDialog() {
        val dialog = BottomSheetDialog(requireActivity())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
        dialog.setCancelable(true)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun isPackageInstalled(packagename: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packagename, 0) //musicplayer.musicapps.music.mp3player
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


//    fun getInstallerPackageName(context: Context, packageName: String) {
//
//        val packages = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
//
//        for (packageInfo in packages) {
//            Log.e("kp", "Package name: ---> " + packageInfo.packageName)
//        }
//
//
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        SpotifyHelper.getInstance(requireContext()).handleSpotifyAuthResponse(requestCode, resultCode, data)
    }
}





