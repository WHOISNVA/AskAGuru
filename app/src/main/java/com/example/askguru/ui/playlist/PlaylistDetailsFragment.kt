package com.example.askguru.ui.playlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.FragmentPlaylistDetailsBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.utils.Const
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.utils.SpotifyCallback
import com.example.askguru.utils.SpotifyHelper
import com.example.askguru.utils.getLongToSeconds
import com.example.askguru.viewmodel.home.HomeVm
import com.example.askguru.viewmodel.home.Recommendation
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track

class PlaylistDetailsFragment : DialogFragment(), AddSongClickListener {

    private lateinit var binding: FragmentPlaylistDetailsBinding
    private val playlistModel by lazy { PlaylistDetailsFragmentArgs.fromBundle(requireArguments()).playlistModel }
    val mainActivity by lazy { requireActivity() }

    //val adapter by lazy { RecommendedListAdapter(mainActivity, playlistModel?.playlist?.recommendations) }

    private val viewModel: HomeVm by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        )[HomeVm::class.java]
    }
    private lateinit var adapter: RecommendedListAdapter


    private var playTrackID: String? = null

    override fun getTheme(): Int {
        return R.style.FullScreenDialogStyle
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
        binding.tvSongBy.text = "By: ${playlistModel?.playlist?.username}"

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

        binding.ivLike.setOnClickListener {
            val token = "Bearer " + PreferenceHelper.getStringPreference(
                requireContext(),
                Const.PRE_AUTHORIZATION_TOKEN
            )
            likeApiCall(token, playlistModel?.playlist?.playlistId!!)
        }


        binding.btnSuggestSongs.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("playlistModel", playlistModel)

            findNavController().navigate(R.id.navigation_add_suggestion, bundle)
        }

        setSpotify()

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


    private fun likeApiCall(token: String, playlistId: String) {
        viewModel.like(token, playlistId).observe(mainActivity) {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {}

                    Status.SUCCESS -> {
                        it.data?.like_count.let {
                            val count = it
                            binding.tvLikeCount.text = "$count"
                        }
                        Toast.makeText(requireContext(), "Liked", Toast.LENGTH_SHORT).show()
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


    private fun setSpotify() {
        playlistModel?.playlist?.spotipyId?.let {
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
            playlistModel?.playlist?.recommendations?.filter { it.spotipyId != null && it.spotipyId != "" }
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
        SpotifyHelper.getInstance(requireContext()).queueSongs(getQueueList())
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
}