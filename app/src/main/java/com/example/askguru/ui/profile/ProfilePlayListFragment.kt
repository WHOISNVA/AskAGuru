package com.example.askguru.ui.profile

import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.askguru.R
import com.example.askguru.databinding.DialogShareBitmapBinding
import com.example.askguru.databinding.FragmentProfilePlayListBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.ui.home.GenreAdapter
import com.example.askguru.ui.home.GenreClickListener
import com.example.askguru.ui.profile_play_list_details.ProfilePlatListDetailsFragment
import com.example.askguru.utils.Const
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.utils.SpotifyCallback
import com.example.askguru.utils.SpotifyHelper
import com.example.askguru.viewmodel.home.GenreModel
import com.example.askguru.viewmodel.home.Recommendation
import com.example.askguru.viewmodel.profile.MyPlaylists
import com.example.askguru.viewmodel.profile.Playlist
import com.example.askguru.viewmodel.profile.Playlists
import com.example.askguru.viewmodel.profile.ProfileVm
import com.google.gson.Gson
import com.google.zxing.WriterException
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class ProfilePlayListFragment() : Fragment(), GenreClickListener,ProfilePlayListClickListener {

    private var myPlayList: ArrayList<MyPlaylists> = arrayListOf()
    val tempList: ArrayList<MyPlaylists> = ArrayList()
    val searchList: ArrayList<MyPlaylists> = ArrayList()
    val backupList: ArrayList<MyPlaylists> = ArrayList()

    private val genreList = ArrayList<GenreModel>()
    private lateinit var genreAdapter: GenreAdapter

    lateinit var binding: FragmentProfilePlayListBinding
    val adapter by lazy { ProfilePlayListAdapter(requireActivity(), myPlayList,this) }

    private lateinit var viewModel: ProfileVm


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val playlistString = PreferenceHelper.getStringPreference(requireContext(), "playlists")
        val playListModel = Gson().fromJson(playlistString, Playlists::class.java)

        myPlayList = playListModel.myPlaylists as ArrayList<MyPlaylists>
        tempList.addAll(myPlayList)
        searchList.addAll(myPlayList)
        backupList.addAll(myPlayList)

        binding = FragmentProfilePlayListBinding.inflate(inflater, container, false)

        setListAdapter()

        viewModel = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[ProfileVm::class.java]

        return binding.root
    }

    private fun setListAdapter() {
        binding.rvProfilePlaylist!!.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvProfilePlaylist!!.addItemDecoration(DividerItemDecoration(binding?.rvProfilePlaylist!!.context,
            (binding.rvProfilePlaylist!!.layoutManager as LinearLayoutManager).orientation))

        binding?.rvProfilePlaylist!!.adapter = adapter
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setGenreListData()

        /*** Filter Module ****/
        binding.tvFilter.setOnClickListener {

            binding.rvGenre.visibility = View.VISIBLE
            binding.llSort.visibility = View.GONE
            binding.llSearch.visibility = View.GONE

            binding.tvFilter.background =  ResourcesCompat.getDrawable(resources, R.drawable.orange_bg, context?.theme)
            binding.tvSort.background =   ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvSearch.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)

            adapter.setListData(tempList)
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

        binding.llCreatePlayList.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("user_id", "")
            bundle.putBoolean("is_ranking",false)
            findNavController().popBackStack()
            findNavController().navigate(R.id.navigation_dashboard,bundle)

        }

        /*** Sort Module Click ****/
        binding.tvMostRecent.setOnClickListener {

            if(myPlayList.isEmpty())
                return@setOnClickListener

            binding.tvMostRecent.background =  ResourcesCompat.getDrawable(resources, R.drawable.orange_bg, context?.theme)
            binding.tvLongest.background =   ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvMostListens.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)

            val sortList: ArrayList<MyPlaylists> = ArrayList()
            sortList.addAll(tempList)
            adapter.setListData(sortList)
        }

        binding.tvLongest.setOnClickListener {

            if(myPlayList.isEmpty())
                return@setOnClickListener

            binding.tvMostRecent.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvLongest.background =   ResourcesCompat.getDrawable(resources, R.drawable.orange_bg, context?.theme)
            binding.tvMostListens.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)

            val sortList: ArrayList<MyPlaylists> = ArrayList()
            sortList.addAll(tempList)
            sortList.sortBy { it.playlist.songDuration }
            sortList.reverse()

            adapter.setListData(sortList)
        }

        binding.tvMostListens.setOnClickListener {

            if(myPlayList.isEmpty())
                return@setOnClickListener

            binding.tvMostRecent.background =  ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvLongest.background =   ResourcesCompat.getDrawable(resources, R.drawable.music_type_bg, context?.theme)
            binding.tvMostListens.background =  ResourcesCompat.getDrawable(resources, R.drawable.orange_bg, context?.theme)

            val sortList: ArrayList<MyPlaylists> = ArrayList()
            sortList.addAll(tempList)
            sortList.sortBy { it.playlist.listens }
            sortList.reverse()

            adapter.setListData(sortList)
        }


        /*** Search Module ****/
        binding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(searchText: Editable?) {


                if(binding.edSearch.text.toString().trim().length>2){
                    filter(binding.edSearch.text.toString().trim())
                }
                else{
                    adapter.setListData(tempList)
                    binding.tvNoData.visibility = View.GONE
                    binding.rvProfilePlaylist.visibility = View.VISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        if(PreferenceHelper.getBooleanPreference(requireContext(),"is_fromRanking_click",false)) {
            binding.llCreatePlayList.visibility = View.GONE
            return
        }else{
            binding.llCreatePlayList.visibility = View.VISIBLE
        }
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
        val filteredlist: ArrayList<MyPlaylists> = ArrayList()
        if(genre == "All"){
            adapter.setListData(tempList)
            binding.tvNoData.visibility = View.GONE
            binding.rvProfilePlaylist.visibility = View.VISIBLE
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
                binding.rvProfilePlaylist.visibility = View.VISIBLE
            }else{
                binding.tvNoData.visibility = View.VISIBLE
                binding.rvProfilePlaylist.visibility = View.GONE

            }
        }
    }


    /**** Search module ****/
    private fun filter(text: String) {
        val filteredlist: ArrayList<MyPlaylists> = ArrayList()
        for (item in myPlayList) {
            if (item.playlist.songTitle.toLowerCase().contains(text.toLowerCase())) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
            binding.rvProfilePlaylist.visibility = View.GONE
        } else {
            Log.e("kp","Filter list size == ${filteredlist.size}")
            adapter.setListData(filteredlist)
            binding.tvNoData.visibility = View.GONE
            binding.rvProfilePlaylist.visibility = View.VISIBLE
        }
    }



    /*** Click Listener ****/
    override fun onPlayListClicked(list: Playlist, position: Int) {

        if(PreferenceHelper.getBooleanPreference(requireContext(),"is_fromRanking_click",false)) {

            return
        }

        val profilePlayListString = Gson().toJson(list)
        PreferenceHelper.setStringPreference(requireContext(), "profile_playlists", profilePlayListString)


/*
        val bundle = Bundle()
        bundle.putSerializable("playlistModel", list)
        //bundle.putBoolean("is_from_ranking",isFromRanking)
        findNavController().navigate(R.id.navigation_profile_play_list_details,bundle)*/

        callDetails(list)
        //setSpotify(list)


//        if (isPackageInstalled("com.apple.android.music", requireActivity().packageManager)) {
//            val openURL = Intent(Intent.ACTION_VIEW)
//            openURL.data = Uri.parse(list.songUrl)
//            startActivity(openURL)
//        } else {
//            val sendIntent = Intent()
//            sendIntent.action = Intent.ACTION_VIEW
//            sendIntent.data = Uri.parse("market://details?id=com.apple.android.music")
//            if (sendIntent.resolveActivity(requireActivity().packageManager) != null) {
//                startActivity(sendIntent)
//            }
//        }
    }

    private fun callDetails(list: Playlist){
        // In PlaylistFragment or wherever you are launching the dialog:
        val dialogFragment = ProfilePlatListDetailsFragment()
        val args = Bundle()
        args.putSerializable("playlistModel", list)
        dialogFragment.arguments = args

// Set the tag for the DialogFragment
        dialogFragment.show(childFragmentManager, "navigation_profile_play_list_details_tag")
    }
    override fun onDeleteClicked(list: Playlist, position: Int, myPlaylists: MyPlaylists) {
        deletePlayListApiCall(list.playlistId,myPlaylists)
    }

    override fun onShareClicked(list: Playlist, position: Int) {
        generateQrCode(list.playlistId)
    }



    private fun deletePlayListApiCall(playlistId: String, list: MyPlaylists) {
        val token = "Bearer "+PreferenceHelper.getStringPreference(requireActivity(), Const.PRE_AUTHORIZATION_TOKEN)

        viewModel.removePlayList(token,playlistId).observe(this, Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {}

                    Status.SUCCESS -> {
                        Log.e("kp","deletePlayListApiCall == Success")
                        myPlayList.remove(list)
                        tempList.remove(list)
                        searchList.remove(list)


                        adapter.notifyDataSetChanged()
                    }

                    Status.ERROR -> {
                        Toast.makeText(requireActivity(), "Some thing went wrong", Toast.LENGTH_LONG).show()
                    }

                }
            }
        })
    }


    private fun isPackageInstalled(packagename: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packagename, 0) //musicplayer.musicapps.music.mp3player
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    private fun generateQrCode(playlistId: String) {
        val manager = requireActivity().getSystemService(WINDOW_SERVICE) as WindowManager?


        val display: Display = manager!!.defaultDisplay
        val point = Point()
        display.getSize(point)


        val width: Int = point.x
        val height: Int = point.y

        var dimen = if (width < height) width else height
        dimen = dimen * 3 / 4

        val qrgEncoder = QRGEncoder(playlistId, null, QRGContents.Type.TEXT, dimen)
        try {
            val bitmap = qrgEncoder.encodeAsBitmap()
            bitmap?.let {
                //shareBitmap(bitmap)

                showShareDialog(bitmap)
            }
        } catch (e: WriterException) { }
    }

    private fun shareBitmap(bitmap: Bitmap) {
        val cachePath: File = File(requireActivity().externalCacheDir, "my_images/")
        cachePath.mkdirs()
        val file = File(cachePath, "${System.currentTimeMillis()}.png")
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }


        val myImageFileUri = FileProvider.getUriForFile(requireActivity(), "com.dapperartisancompany.askaguru.provider", file)

        //create a intent

        //create a intent
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_STREAM, myImageFileUri)
        intent.putExtra(Intent.EXTRA_TEXT, "Here is My PlayList")
        intent.type = "image/png"
        startActivity(Intent.createChooser(intent, "Share with"))
        //startActivity(intent);
    }

    fun showShareDialog(bitmap: Bitmap) {

        val dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_share_bitmap, null)
        val dialogBuilder = AlertDialog.Builder(requireActivity()).setView(dialogView)
        val dialogBinding = DialogShareBitmapBinding.bind(dialogView)



        dialogBinding.ivBitmap.setImageBitmap(bitmap)

        val customAlertDialog = dialogBuilder.show()
        customAlertDialog.setCancelable(false)

        customAlertDialog.window?.decorView?.setBackgroundResource(R.drawable.dialog_background)

        dialogBinding.btnShare.setOnClickListener {
            customAlertDialog.dismiss()
            shareBitmap(bitmap)
        }

        dialogBinding.btnCancel.setOnClickListener {
            customAlertDialog.dismiss()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //SpotifyHelper.getInstance(requireContext()).handleSpotifyAuthResponse(requestCode, resultCode, data)
        val dialogFragment = childFragmentManager.findFragmentByTag("navigation_profile_play_list_details_tag") as? ProfilePlatListDetailsFragment
        dialogFragment?.onActivityResult(requestCode, resultCode, data)
    }
}