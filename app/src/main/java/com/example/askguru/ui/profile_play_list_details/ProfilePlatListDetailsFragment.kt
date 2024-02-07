package com.example.askguru.ui.profile_play_list_details

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.FragmentProfilePlatListDetailsBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.ui.recommendation.RecommendationAdapter
import com.example.askguru.ui.recommendation.RecommendationListener
import com.example.askguru.utils.*
import com.example.askguru.viewmodel.add_song.AddSongVm
import com.example.askguru.viewmodel.add_song.AddSuggestionRequest
import com.example.askguru.viewmodel.add_song.NewRecommendation
import com.example.askguru.viewmodel.add_song.SearchList
import com.example.askguru.viewmodel.home.Recommendation
import com.example.askguru.viewmodel.notification.NotificationsViewModel
import com.example.askguru.viewmodel.profile.Playlist
import com.google.gson.Gson
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import kotlinx.coroutines.launch


class ProfilePlatListDetailsFragment : DialogFragment(), RecommendationListener {

    private lateinit var binding : FragmentProfilePlatListDetailsBinding

    private val recommendationList: ArrayList<Recommendation> = ArrayList()
    private lateinit var adapter: RecommendationAdapter
    private lateinit var viewModel: NotificationsViewModel

    override fun getTheme(): Int = R.style.FullScreenDialogStyle

    private var isRecommendationListClick = false
    private var recommendationListClickPosition = -1
    private var recommendationListClickedSpotifyId = ""

    private var askGuruTrack: AskGuruTrack? = null

    private lateinit var addSongsVM: AddSongVm
    private var myPlayListModel : Playlist? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfilePlatListDetailsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[NotificationsViewModel::class.java]
        addSongsVM = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[AddSongVm::class.java]

        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle)


        val playlistString = PreferenceHelper.getStringPreference(requireContext(), "profile_playlists")
        val playListModel = Gson().fromJson(playlistString, Playlist::class.java)
        myPlayListModel = playListModel


        binding.ivCross.setOnClickListener {
            PreferenceHelper.setStringPreference(requireContext(), "profile_playlists","")
            findNavController().popBackStack()

        }

        recommendationList.addAll(playListModel.recommendations)


        Glide.with(binding.ivImage.context).load(playListModel.artwork).into(binding.ivImage)

        binding.tvTitle.text = playListModel.songTitle
        binding.tvSongBy.text = "By: ${playListModel.artistName}"

        recommendationList.let {
            binding.tvLikeCount.text = "${playListModel.playlistLikeCount}"
        }

        playListModel.listens.let {
            binding.tvPlayCount.text = "${playListModel.listens}"
        }

        var totalMinutes = 0
        playListModel.recommendations?.forEach {
            totalMinutes += it.songDuration
        }
        totalMinutes = totalMinutes.toLong().getLongToSeconds()
        val stringTrack = if (playListModel.recommendations?.size!! > 1) "tracks" else "track"
        binding.tvTotalTrackAndTime.text = "${playListModel.recommendations!!.size} $stringTrack, $totalMinutes minutes"



        adapter = RecommendationAdapter(recommendationList,this)
        binding.rvSongs!!.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvSongs!!.adapter = adapter



        binding.ivLike.visibility = View.GONE
        binding.ivLike.setOnClickListener {
//            val token = "Bearer " + PreferenceHelper.getStringPreference(requireContext(), Const.PRE_AUTHORIZATION_TOKEN)
//            likeApiCall(token, playListModel.playlistId!!)
        }

        val navController = activity?.findNavController(R.id.nav_host_fragment_activity_main)
        binding.btnAddSongs.setOnClickListener {
            /*val bundle = Bundle()
            bundle.putString("user_id", "")
            bundle.putBoolean("is_ranking",false)
            findNavController().popBackStack()
            findNavController().navigate(R.id.navigation_dashboard,bundle)*/

            navController?.navigate(R.id.add_song)
        }
        findNavController().currentBackStackEntry
            ?.savedStateHandle?.let { handle ->
                handle.getLiveData<ArrayList<SearchList>>("list")
                    .observe(viewLifecycleOwner) { res ->
                        addToPlayListAsRecommendation(res)
                    }
            }

        binding.btnDeletePlayList.setOnClickListener {
            deletePlayListApiCall(playListModel.playlistId)
        }

        if(!PreferenceHelper.getBooleanPreference(requireContext(),"is_show_delete_button",true)){
            binding.btnDeletePlayList.visibility = View.GONE
        }else{
            binding.btnDeletePlayList.visibility = View.VISIBLE
        }

        setSpotify(playListModel)
        binding.ivPlay.setOnClickListener {
            askGuruTrack?.let {
                if(!isRecommendationListClick){
                    if (SpotifyHelper.getInstance(requireActivity()).isPaused) {
                        SpotifyHelper.getInstance(requireActivity()).resume()
                    } else {
                        SpotifyHelper.getInstance(requireActivity()).pause()
                    }
                }else{
                    isRecommendationListClick = false
                    setSpotify(playListModel)
                }
            } ?: kotlin.run {
                Toast.makeText(requireContext(), "can't play", Toast.LENGTH_SHORT).show()
            }
        }

        binding.includedPlaying.imgPlayPause.setOnClickListener {
            askGuruTrack?.let {
                if (SpotifyHelper.getInstance(requireActivity()).isPaused) {
                    SpotifyHelper.getInstance(requireActivity()).resume()
                } else {
                    SpotifyHelper.getInstance(requireActivity()).pause()
                }
            } ?: kotlin.run {
                Toast.makeText(requireContext(), "Can't play", Toast.LENGTH_SHORT).show()
            }
        }
        binding.includedPlaying.imgNext.setOnClickListener {
            isRecommendationListClick = true
            SpotifyHelper.getInstance(requireActivity()).playNext()
        }
        binding.includedPlaying.imgPrevious.setOnClickListener {
            isRecommendationListClick = true
            SpotifyHelper.getInstance(requireActivity()).playPrevious()
        }
        observer()
    }

    private fun addToPlayListAsRecommendation(newItems: ArrayList<SearchList>) {
        val recommendationList: ArrayList<NewRecommendation> = ArrayList()

        Log.d("addToPlayListAsRecommendation","list size -- ${newItems.size}")
        newItems.forEachIndexed { index, searchList ->
            Log.d("addToPlayListAsRecommendation","$index => ${searchList.id} => ${searchList.attributes.albumName}")
            //call api in loop
            recommendationList.add(NewRecommendation(searchList.id))

        }
        val requestModel = AddSuggestionRequest(recommendationList)
        myPlayListModel?.playlistId?.let {
            addRecommendationApiCall(requestModel,it)
        }

    }

    private fun addRecommendationApiCall(requestModel: AddSuggestionRequest, playListId: String) {
        val token = "Bearer ${PreferenceHelper.getStringPreference(requireContext(), Const.PRE_AUTHORIZATION_TOKEN)}"
        addSongsVM.addRecommendation(token,playListId,requestModel).observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progress.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        try {
                            binding.progress.visibility = View.GONE
                            Toast.makeText(requireContext(),"Song added Successfully",Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                            //dismiss()
                        }catch (e : Exception){
                        }
                    }
                    Status.ERROR -> {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(requireContext(),"Something went wrong",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun observer(){
        lifecycleScope.launch {
            SpotifyHelper.getInstance(requireActivity()).currentAskGuruTrack.collect { askGuruTrack ->
                askGuruTrack?.let {
                    binding.includedPlaying.mainLayout.visibility = View.VISIBLE
                    binding.includedPlaying.tvTitle.text = askGuruTrack.title
                    binding.includedPlaying.tvSubTitle.text = askGuruTrack.subTitle
                    askGuruTrack.imageUrl?.let {
                        Glide.with(requireContext()).load(askGuruTrack.imageUrl)
                            .into(binding.includedPlaying.imgTrack)
                    }
                } ?: kotlin.run {
                    binding.includedPlaying.mainLayout.visibility = View.GONE
                }
            }
        }
    }

    override fun onClickAccepted(list: Recommendation, position: Int) {
        Log.e("kp","id -- ${list.recommendationId}")
        val token = "Bearer ${PreferenceHelper.getStringPreference(requireContext(), Const.PRE_AUTHORIZATION_TOKEN)}"
        acceptRequestApiCall(token,list.recommendationId,position)
    }

    override fun onClickRejected(list: Recommendation, position: Int) {
        val token = "Bearer ${PreferenceHelper.getStringPreference(requireContext(), Const.PRE_AUTHORIZATION_TOKEN)}"
        rejectRequestApiCall(token,list.recommendationId,position)
    }

    override fun onItemClick(list: Recommendation, position: Int) {
        //Toast.makeText(requireContext(), "Playing - ${list.songTitle}", Toast.LENGTH_SHORT).show()
        recommendationListClick(clickPos = position,clickedSpotifyId = list.spotipyId.toString())
    }
    private fun acceptRequestApiCall(token: String, recommendationId: String, position: Int) {
        viewModel.acceptedRequest(token,recommendationId).observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progress.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.progress.visibility = View.GONE
                        recommendationList.removeAt(position)
                        adapter.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "Request Accepted", Toast.LENGTH_LONG).show()
                    }
                    Status.ERROR -> {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(requireContext(), "Something went wrong try again", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun rejectRequestApiCall(token: String, recommendationId: String, position: Int) {
        viewModel.rejectRequest(token,recommendationId).observe(requireActivity(), Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progress.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.progress.visibility = View.GONE
                        recommendationList.removeAt(position)
                        adapter.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "Request Rejected", Toast.LENGTH_LONG).show()
                    }
                    Status.ERROR -> {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(requireContext(), "Something went wrong try again", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun deletePlayListApiCall(playlistId: String) {
        val token = "Bearer "+PreferenceHelper.getStringPreference(requireActivity(), Const.PRE_AUTHORIZATION_TOKEN)

        viewModel.removePlayList(token,playlistId).observe(this, Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> { binding.progress.visibility = View.VISIBLE }

                    Status.SUCCESS -> {
                        binding.progress.visibility = View.GONE
                        PreferenceHelper.setStringPreference(requireContext(), "profile_playlists","")
                        findNavController().popBackStack()
                    }

                    Status.ERROR -> {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(requireActivity(), "Some thing went wrong", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun setSpotify(playlist : Playlist?) {
        playlist?.spotipyId?.let {
            //askGuruTrack = "spotify:track:$it"
            askGuruTrack = AskGuruTrack(
                title = playlist.songTitle,
                spotifyID = "spotify:track:$it",
                imageUrl = playlist.artwork,
                subTitle = playlist.artistName
            )
            SpotifyHelper.getInstance(requireActivity()).setCallback(callback = storyOptionCallBack)
            SpotifyHelper.getInstance(requireActivity()).authenticateSpotify()
        } ?: kotlin.run {
            Toast.makeText(requireContext(), "can't play", Toast.LENGTH_SHORT).show()
        }
    }

    private fun recommendationListClick(clickPos : Int, clickedSpotifyId : String){
        isRecommendationListClick = true
        recommendationListClickPosition = clickPos
        recommendationListClickedSpotifyId = "spotify:track:$clickedSpotifyId"

        Log.d("SpotifyHelper","recommendationListClick $isRecommendationListClick , $recommendationListClickPosition, $recommendationListClickedSpotifyId")
        SpotifyHelper.getInstance(requireActivity()).setCallback(callback = storyOptionCallBack)
        SpotifyHelper.getInstance(requireActivity()).authenticateSpotify()
    }

    private fun startPlaying() {
        if(isRecommendationListClick){
            SpotifyHelper.getInstance(requireActivity()).playRecommendationListClick(recommendationListClickedSpotifyId)
        }else{
            newPlaying()
        }
    }

    private fun newPlaying(){
        val songList = mutableListOf<AskGuruTrack>()
        askGuruTrack?.let {
            songList.add(it)
        }
        val recommendation =  recommendationList.filter { it.spotipyId != null && it.spotipyId != "" }
        recommendation.forEachIndexed { index, recommendation ->
            recommendation.spotipyId?.let {
                //songList.add("spotify:track:$it")
                val item = AskGuruTrack(
                    title = recommendation.songTitle,
                    spotifyID = "spotify:track:$it",
                    imageUrl = recommendation.artwork,
                    subTitle = recommendation.artistName
                )
                songList.add(item)
            }
        }

        /*songList.add("spotify:track:7tTjNyAdEopOYLJ8yttDWM")//ABC (Alphabet Song)
        songList.add("spotify:track:45x3yuEpjmiqL4SFdT4srk")//Test Me
        songList.add("spotify:track:1DMEzmAoQIikcL52psptQL")//test drive
        songList.add("spotify:track:0hkPWDyQoqiRiLSI535oZJ")//ABC (Alphabet Song)*/

        SpotifyHelper.getInstance(requireActivity()).setUpPlayList(songList)
    }

    private val storyOptionCallBack = object : SpotifyCallback {
        override fun onAlreadyAuthenticated() {
            startPlaying()
        }

        override fun onAuthSuccess(accessToken: String) {
            val isConnected = SpotifyHelper.getInstance(requireActivity()).isConnected()
            if (isConnected) {
                startPlaying()
            } else {
                SpotifyHelper.getInstance(requireActivity()).connectSpotify()
            }
        }

        override fun onAuthFailure(error: String) {
            Toast.makeText(requireContext(), "Auth - $error", Toast.LENGTH_SHORT).show()
        }

        override fun onConnectedSuccess(appRemote: SpotifyAppRemote) {
            startPlaying()
        }

        override fun onConnectedError(error: String) {
            Toast.makeText(requireContext(), "Connection - $error", Toast.LENGTH_SHORT).show()
        }

        override fun onPause(track: Track) {
            //Toast.makeText(requireContext(), "onPause", Toast.LENGTH_SHORT).show()
        }

        override fun onPlay(track: Track) {
            Toast.makeText(requireContext(), "onPlay()", Toast.LENGTH_SHORT).show()
        }

        override fun onTrackStatusChange(playerState: PlayerState) {
            /*Log.d(
                "SpotifyHelper",
                "Playerdetailview playTrack track isPaused ${playerState.isPaused}"
            )*/
            playerState?.let {
                it.track?.let {
                    //Log.d("SpotifyHelper", "Playerdetailview track url = ${it.uri}")
                    if (it.uri.toString().equals(askGuruTrack?.spotifyID)) {
                        if (playerState.isPaused) {
                            binding.ivPlay.setImageResource(R.drawable.play)
                        } else {
                            binding.ivPlay.setImageResource(R.drawable.pause)
                        }
                    }else{
                        binding.ivPlay.setImageResource(R.drawable.play)
                    }
                    if (playerState.isPaused) {
                        binding.includedPlaying.imgPlayPause.setImageResource(R.drawable.play)
                    } else {
                        binding.includedPlaying.imgPlayPause.setImageResource(R.drawable.pause)
                    }
                }
            }
        }

        override fun onPlaybackError(error: String) {
            Toast.makeText(requireContext(), "Play Error - $error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        SpotifyHelper.getInstance(requireActivity()).handleSpotifyAuthResponse(requestCode, resultCode, data)
    }


}