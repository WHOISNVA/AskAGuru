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
import com.example.askguru.utils.Const
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.utils.SpotifyCallback
import com.example.askguru.utils.SpotifyHelper
import com.example.askguru.utils.getLongToSeconds
import com.example.askguru.viewmodel.home.Recommendation
import com.example.askguru.viewmodel.notification.NotificationsViewModel
import com.example.askguru.viewmodel.profile.Playlist
import com.google.gson.Gson
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track


class ProfilePlatListDetailsFragment : DialogFragment(), RecommendationListener {

    private lateinit var binding : FragmentProfilePlatListDetailsBinding

    val recommendationList: ArrayList<Recommendation> = ArrayList()
    private lateinit var adapter: RecommendationAdapter
    private lateinit var viewModel: NotificationsViewModel

    override fun getTheme(): Int = R.style.FullScreenDialogStyle

    private var playTrackID: String? = null
    private var playRecomndaded  = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfilePlatListDetailsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService)))[NotificationsViewModel::class.java]

        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle)


        val playlistString = PreferenceHelper.getStringPreference(requireContext(), "profile_playlists")
        val playListModel = Gson().fromJson(playlistString, Playlist::class.java)



        binding.ivCross.setOnClickListener {
            PreferenceHelper.setStringPreference(requireContext(), "profile_playlists","")
            findNavController().popBackStack()

        }

        recommendationList.addAll(playListModel.recommendations)


        Glide.with(binding.ivImage.context).load(playListModel.artwork).into(binding.ivImage)

        binding.tvTitle.text = playListModel.songTitle
        binding.tvSongBy.text = "By: ${playListModel.username}"

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

        binding.btnAddSongs.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("user_id", "")
            bundle.putBoolean("is_ranking",false)
            findNavController().popBackStack()
            findNavController().navigate(R.id.navigation_dashboard,bundle)
        }

        binding.btnDeletePlayList.setOnClickListener {
            deletePlayListApiCall(playListModel.playlistId)
        }

        if(!PreferenceHelper.getBooleanPreference(requireContext(),"is_show_delete_button",true)){
            binding.btnDeletePlayList.visibility = View.GONE
        }else{
            binding.btnDeletePlayList.visibility = View.VISIBLE
        }

        setSpotify(playListModel.spotipyId)
        binding.ivPlay.setOnClickListener {
            playTrackID?.let {
                if (SpotifyHelper.getInstance(requireContext()).isPaused) {
                    SpotifyHelper.getInstance(requireContext()).resume()
                } else {
                    SpotifyHelper.getInstance(requireContext()).pause()
                }
            } ?: kotlin.run {
                Toast.makeText(requireContext(), "can't play", Toast.LENGTH_SHORT).show()
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
        setSpotify(list.spotipyId,false)
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

    private fun setSpotify(spotifyID : String?,playRecomnd : Boolean = true) {
        spotifyID?.let {
            playRecomndaded = playRecomnd
            playTrackID = "spotify:track:$it"
            SpotifyHelper.getInstance(requireContext()).setCallback(callback = storyOptionCallBack)
            SpotifyHelper.getInstance(requireContext()).authenticateSpotify()
        } ?: kotlin.run {
            Toast.makeText(requireContext(), "can't play", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getQueueList(): List<String> {
        val queueList = mutableListOf<String>()

        val recomnation =
            recommendationList?.filter { it.spotipyId != null && it.spotipyId != "" }
                ?: mutableListOf()
        Log.d("getQueueList", "getQueueList recomnation = ${recomnation?.size} $recomnation")
        /*if (recomnation.isEmpty()) {
            Log.d("getQueueList", "added static recomndation")
            queueList.add("7tTjNyAdEopOYLJ8yttDWM")//ABC (Alphabet Song)
            queueList.add("45x3yuEpjmiqL4SFdT4srk")//Test Me
            queueList.add("1DMEzmAoQIikcL52psptQL")//test drive
            queueList.add("0hkPWDyQoqiRiLSI535oZJ")//ABC (Alphabet Song)
        } else {
            Log.d("getQueueList", "added default recomndation")
            recomnation?.forEachIndexed { index, recommendation ->
                recommendation.spotipyId?.let {
                    queueList.add(it)
                }
            }
        }*/

        recomnation?.forEachIndexed { index, recommendation ->
            recommendation.spotipyId?.let {
                queueList.add(it)
            }
        }
        return queueList
    }

    private fun startPlaying() {
        SpotifyHelper.getInstance(requireContext()).playTrack(playTrackID!!)
        if(playRecomndaded){
            SpotifyHelper.getInstance(requireContext()).queueSongs(getQueueList())
        }
    }

    private val storyOptionCallBack = object : SpotifyCallback {
        override fun onAlreadyAuthenticated() {
            startPlaying()
        }

        override fun onAuthSuccess(accessToken: String) {
            val isConnected = SpotifyHelper.getInstance(requireContext()).isConnected()
            if (isConnected) {
                startPlaying()
            } else {
                SpotifyHelper.getInstance(requireContext()).connectSpotify()
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
            Log.d(
                "SpotifyHelper",
                "Playerdetailview playTrack track isPaused ${playerState.isPaused}"
            )
            playerState?.let {
                it.track?.let {
                    Log.d("SpotifyHelper", "Playerdetailview track url = ${it.uri}")
                    if (it.uri.toString().equals(playTrackID)) {
                        if (playerState.isPaused) {
                            binding.ivPlay.setImageResource(R.drawable.play)
                        } else {
                            binding.ivPlay.setImageResource(R.drawable.pause)
                        }
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
        SpotifyHelper.getInstance(requireContext()).handleSpotifyAuthResponse(requestCode, resultCode, data)
    }
}