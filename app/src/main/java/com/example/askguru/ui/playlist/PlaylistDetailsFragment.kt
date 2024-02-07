package com.example.askguru.ui.playlist

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.FragmentPlaylistDetailsBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.ui.profile.LikedPlaylists
import com.example.askguru.utils.*
import com.example.askguru.viewmodel.home.HomeVm
import com.example.askguru.viewmodel.home.Recommendation
import com.example.askguru.viewmodel.profile.ProfileVm
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import kotlinx.coroutines.launch

class PlaylistDetailsFragment : DialogFragment(), AddSongClickListener {

    private lateinit var binding: FragmentPlaylistDetailsBinding

    //private var playlistModel : PlayListResponseItem? = null
    private val playlistModel by lazy { PlaylistDetailsFragmentArgs.fromBundle(requireArguments()).playlistModel }

    val mainActivity by lazy { requireActivity() }

    //val adapter by lazy { RecommendedListAdapter(mainActivity, playlistModel?.playlist?.recommendations) }

    private var isRecommendationListClick = false
    private var recommendationListClickPosition = -1
    private var recommendationListClickedSpotifyId = ""
    private var isRandomPlaying = false

    private val viewModel: HomeVm by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        )[HomeVm::class.java]
    }
    private lateinit var adapter: RecommendedListAdapter


    private var askGuruTrack: AskGuruTrack? = null

    override fun getTheme(): Int {
        return R.style.FullScreenDialogStyle
    }

    private var isLiked = false

    private val profileVewModel: ProfileVm by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        )[ProfileVm::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistDetailsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle)
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()

        }
        Log.d("DetailView", "spotipyId ${playlistModel?.playlist?.spotipyId}")

        Glide.with(binding.ivImage.context).load(playlistModel?.playlist?.artwork)
            .into(binding.ivImage)

        binding.tvTitle.text = playlistModel?.playlist?.songTitle
        binding.tvSongBy.text = "By: ${playlistModel?.playlist?.artistName}"

        playlistModel?.playlist?.playlistLikeCount.let {
            binding.tvLikeCount.text = "${playlistModel?.playlist?.playlistLikeCount}"
        }

        playlistModel?.playlist?.listens.let {
            binding.tvPlayCount.text = "${playlistModel?.playlist?.listens}"
        }

        var totalMinutes = 0
        playlistModel?.playlist?.recommendations?.forEach {
            totalMinutes += it.songDuration
        }
        totalMinutes = totalMinutes.toLong().getLongToSeconds()
        val stringTrack =
            if (playlistModel?.playlist?.recommendations?.size!! > 1) "tracks" else "track"
        binding.tvTotalTrackAndTime.text =
            "${playlistModel?.playlist?.recommendations!!.size} $stringTrack, $totalMinutes minutes"


//        binding.rvSongs.adapter = adapter
        adapter =
            RecommendedListAdapter(mainActivity, playlistModel?.playlist?.recommendations, this)
        binding.rvSongs!!.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvSongs!!.adapter = adapter

        binding.btnSuggestSongs.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("playlistModel", playlistModel)

            findNavController().navigate(R.id.navigation_add_suggestion, bundle)
        }

        setSpotify()

        binding.ivPlay.setOnClickListener {
            askGuruTrack?.let {
                if (!isRecommendationListClick) {
                    if (SpotifyHelper.getInstance(requireActivity()).isPaused) {
                        SpotifyHelper.getInstance(requireActivity()).resume()
                    } else {
                        SpotifyHelper.getInstance(requireActivity()).pause()
                    }
                } else {
                    isRecommendationListClick = false
                    setSpotify()
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

        getProfileDataApiCall()
        updateLike()
        binding.ivLike.setOnClickListener {
            val token = PreferenceHelper.getStringPreference(
                requireContext(),
                Const.PRE_AUTHORIZATION_TOKEN
            )

            token?.let {
                val fullToken = "Bearer " + it
                likeApiCall(fullToken, playlistModel?.playlist?.playlistId!!)
            } ?: kotlin.run {
                Toast.makeText(requireContext(), "Please login", Toast.LENGTH_SHORT).show()
            }
        }

        binding.iconAskGuru.setOnClickListener {
            viewModel.getAllPlayListRandom()
        }
        observer()
    }

    private fun observer() {
        viewModel.playlistsLiveData.observe(this) {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        viewModel.getRandomSong()
                    }

                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Some thing went wrong",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
        }

        viewModel.randomPlaylist.observe(this) {
            isRandomPlaying = true
            //play random playlist here
            Log.d("randomPlaylist", "$it")
            playlistModel?.playlist = it
            setSpotify()
        }

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


    private fun updateLike() {
        if (isLiked) {
            //binding.ivLike.setImageResource(R.drawable.baseline_favorite_24)
            val tintColor = ContextCompat.getColor(requireContext(), R.color.like_selected)
            binding.ivLike.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        } else {
            //binding.ivLike.setImageResource(R.drawable.empty_heart)
            val tintColor = ContextCompat.getColor(requireContext(), R.color.like_deselected)
            binding.ivLike.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun getProfileDataApiCall() {
        val token =
            PreferenceHelper.getStringPreference(requireContext(), Const.PRE_AUTHORIZATION_TOKEN)
        Log.e("kp", "token ==> $token")
        token?.let {
            profileVewModel.getProfileData("Bearer $token").observe(requireActivity()) { it ->
                it.let { resource ->
                    when (resource.status) {
                        Status.LOADING -> {}
                        Status.SUCCESS -> {
                            it.data?.let {
                                isLiked = hasLiked(it.playlists.liked_playlists)
                                updateLike()
                            }
                        }

                        Status.ERROR -> {
                            Toast.makeText(
                                requireActivity(), "Some thing went wrong", Toast.LENGTH_LONG
                            ).show()
                        }

                    }
                }
            }
        }

    }

    private fun hasLiked(likedPlaylists: List<LikedPlaylists>): Boolean {
        return likedPlaylists.any { it.playlist.playlist_id == playlistModel?.playlist?.playlistId }
    }


    private fun likeApiCall(token: String, playlistId: String) {
        viewModel.like(token, playlistId, isLiked).observe(mainActivity) {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {}

                    Status.SUCCESS -> {
                        it.data?.let {
                            it.like_count.let {
                                val count = it
                                binding.tvLikeCount.text = "$count"
                            }
                            if (it.status == 200) {
                                Toast.makeText(requireContext(), "Liked", Toast.LENGTH_SHORT).show()
                                isLiked = true
                            } else {
                                Toast.makeText(requireContext(), "Disliked", Toast.LENGTH_SHORT)
                                    .show()
                                isLiked = false
                            }
                            updateLike()
                        }
                    }

                    Status.ERROR -> {
                        Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    override fun onAddSongClick(model: Recommendation?, position: Int) {
        //val navController = activity?.findNavController(R.id.nav_host_fragment_activity_main)
        //navController?.navigate(R.id.navigation_dashboard)

        PreferenceHelper.setIntegerPreference(requireActivity(), "pos", position)

        val bundle = Bundle()
        bundle.putSerializable("playlistModel", playlistModel)
        bundle.putInt("pos", position)
        findNavController().navigate(R.id.navigation_dashboard, bundle)
    }

    override fun onItemClick(list: Recommendation?, position: Int) {
        list?.let {
           /* Toast.makeText(requireContext(), "Playing - ${list.songTitle}", Toast.LENGTH_SHORT)
                .show()*/
            recommendationListClick(
                clickPos = position,
                clickedSpotifyId = list.spotipyId.toString()
            )
        } ?: kotlin.run {
            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setSpotify() {
        playlistModel?.playlist?.spotipyId?.let {
            //askGuruTrack = "spotify:track:$it"
            askGuruTrack = AskGuruTrack(
                title = playlistModel?.playlist?.songTitle ?: "",
                spotifyID = "spotify:track:$it",
                imageUrl = playlistModel?.playlist?.artwork ?: "",
                subTitle = playlistModel?.playlist?.artistName ?: ""
            )
            SpotifyHelper.getInstance(requireActivity()).setCallback(callback = storyOptionCallBack)
            SpotifyHelper.getInstance(requireActivity()).authenticateSpotify()

           /* Toast.makeText(
                requireContext(),
                "Playing - ${playlistModel?.playlist?.songTitle}",
                Toast.LENGTH_SHORT
            ).show()*/
        } ?: kotlin.run {
            Toast.makeText(requireContext(), "can't play", Toast.LENGTH_SHORT).show()
        }
    }

    private fun recommendationListClick(clickPos: Int, clickedSpotifyId: String) {
        if(isRandomPlaying){
            Log.d("SpotifyHelper","Can't click, as random song is playing is playing")
            return
        }
        isRecommendationListClick = true
        recommendationListClickPosition = clickPos
        recommendationListClickedSpotifyId = "spotify:track:$clickedSpotifyId"

        Log.d(
            "SpotifyHelper",
            "recommendationListClick $isRecommendationListClick , $recommendationListClickPosition, $recommendationListClickedSpotifyId"
        )
        SpotifyHelper.getInstance(requireActivity()).setCallback(callback = storyOptionCallBack)
        SpotifyHelper.getInstance(requireActivity()).authenticateSpotify()
    }

    private fun startPlaying() {
        /*   SpotifyHelper.getInstance(requireContext()).playTrack(playTrackID!!)
           SpotifyHelper.getInstance(requireContext()).queueSongs(getQueueList())*/

        if (isRecommendationListClick) {
            SpotifyHelper.getInstance(requireActivity()) .playRecommendationListClick(recommendationListClickedSpotifyId)
        } else {
            newPlaying()
        }
    }

    private fun newPlaying() {
        val songList = mutableListOf<AskGuruTrack>()
        askGuruTrack?.let {
            songList.add(it)
        }
        val recommendation =
            playlistModel?.playlist?.recommendations?.filter { it.spotipyId != null && it.spotipyId != "" }
        recommendation?.forEachIndexed { index, recommendation ->
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
            /* Log.d(
                 "SpotifyHelper",
                 "Playerdetailview playTrack track isPaused ${playerState.isPaused}"
             )*/
            playerState?.let {
                it.track?.let {
                    //     Log.d("SpotifyHelper", "Playerdetailview track url = ${it.uri}")
                    if (it.uri.toString().equals(askGuruTrack?.spotifyID)) {
                        if (playerState.isPaused) {
                            binding.ivPlay.setImageResource(R.drawable.play)
                        } else {
                            binding.ivPlay.setImageResource(R.drawable.pause)
                        }
                    } else {
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
}