package com.example.askguru.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.askguru.R
import com.example.askguru.databinding.ActivityMainBinding
import com.example.askguru.network.ApiHelper
import com.example.askguru.network.RetrofitBuilder
import com.example.askguru.network.Status
import com.example.askguru.network.ViewModelFactory
import com.example.askguru.ui.login.LoginActivity
import com.example.askguru.ui.qr.ScannerActivity
import com.example.askguru.utils.AskGuruTrack
import com.example.askguru.utils.Const.Companion.PRE_IS_LOGIN
import com.example.askguru.utils.PreferenceHelper
import com.example.askguru.utils.SpotifyCallback
import com.example.askguru.utils.SpotifyHelper
import com.example.askguru.viewmodel.home.HomeVm
import com.example.askguru.viewmodel.home.Playlist
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.permissionx.guolindev.PermissionX
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: HomeVm

    private var askGuruTrack: AskGuruTrack? = null
    private var playlistModel: Playlist? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //ViewModelProvider(this).get(HomeViewModel::class.java)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        )[HomeVm::class.java]


        if (PreferenceHelper.getBooleanPreference(this, PRE_IS_LOGIN, false))
            binding.tvLogin.visibility = View.GONE

        val navView: BottomNavigationView = binding.navView


        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_notifications,
                R.id.navigation_dashboard,
                R.id.navigation_ranking,
                R.id.navigation_profile_data,
            )
        )

        navView.setupWithNavController(navController)



        if (PreferenceHelper.getBooleanPreference(this, PRE_IS_LOGIN, false)) {
            binding.tvLogin.visibility = View.GONE
            binding.ivScanner.visibility = View.VISIBLE
        } else {
            binding.tvLogin.visibility = View.VISIBLE
            binding.ivScanner.visibility = View.GONE
        }

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


        binding.ivScanner.setOnClickListener {
            PermissionX.init(this)
                .permissions(Manifest.permission.CAMERA)
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Core fundamental are based on these permissions",
                        "OK",
                        "Cancel"
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        openScannerScreen()
                    } else {
                        Toast.makeText(
                            this,
                            "These permissions are denied: $deniedList",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }



        navController.addOnDestinationChangedListener { _, destination, _ ->


            when (destination.id) {


                R.id.navigation_search -> {
                    binding.appBarLayout.visibility = View.GONE
                }

                R.id.navigation_notifications -> {
                    binding.appBarLayout.visibility = View.GONE
                }

                R.id.navigation_settings -> {
                    binding.appBarLayout.visibility = View.GONE
                }

                R.id.navigation_create_list -> {
                    binding.appBarLayout.visibility = View.GONE
                }

                R.id.navigation_full_list -> {
                    binding.appBarLayout.visibility = View.GONE
                }

                R.id.navigation_ranking -> {
                    if (PreferenceHelper.getBooleanPreference(
                            this,
                            "is_fromRanking_click",
                            false
                        )
                    ) {
                        PreferenceHelper.setBooleanPreference(this, "is_fromRanking_click", false)
                        navController.popBackStack()
                    }
                    binding.appBarLayout.visibility = View.GONE
                }

                R.id.navigation_profile_data -> {
                    if (PreferenceHelper.getBooleanPreference(
                            this,
                            "is_fromRanking_click",
                            false
                        )
                    ) {
                        PreferenceHelper.setBooleanPreference(this, "is_fromRanking_click", false)
                        binding.appBarLayout.visibility = View.VISIBLE
                        navController.popBackStack()
                    } else
                        binding.appBarLayout.visibility = View.GONE
                }

                R.id.navigation_dashboard -> {
                    binding.appBarLayout.visibility = View.GONE
                }

                R.id.add_song -> {
                    binding.appBarLayout.visibility = View.GONE
                }

                R.id.navigation_playlist_details -> {
                    binding.appBarLayout.visibility = View.GONE
                }

                else -> {
                    binding.appBarLayout.visibility = View.VISIBLE
                }
            }
        }

        //SpotifyHelper.getInstance(this).setCallback(callback = storyOptionCallBack)
        //SpotifyHelper.getInstance(this).authenticateSpotify()

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
                            this,
                            "Some thing went wrong",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
        }

        viewModel.randomPlaylist.observe(this) {
            //play random playlist here
            Log.d("randomPlaylist", "$it")
            setSpotify(it)
        }

        binding.includedPlaying.imgPlayPause.setOnClickListener {
            if (SpotifyHelper.getInstance(this@MainActivity).isPaused) {
                SpotifyHelper.getInstance(this@MainActivity).resume()
            } else {
                SpotifyHelper.getInstance(this@MainActivity).pause()
            }
        }

        binding.includedPlaying.imgNext.setOnClickListener {
            SpotifyHelper.getInstance(this@MainActivity).playNext()
        }
        binding.includedPlaying.imgPrevious.setOnClickListener {
            SpotifyHelper.getInstance(this@MainActivity).playPrevious()
        }

        lifecycleScope.launch {
            SpotifyHelper.getInstance(this@MainActivity).currentAskGuruTrack.collect { askGuruTrack ->
                askGuruTrack?.let {
                    binding.includedPlaying.mainLayout.visibility = View.VISIBLE
                    binding.includedPlaying.tvTitle.text = askGuruTrack.title
                    binding.includedPlaying.tvSubTitle.text = askGuruTrack.subTitle
                    askGuruTrack.imageUrl?.let {
                        Glide.with(this@MainActivity).load(askGuruTrack.imageUrl)
                            .into(binding.includedPlaying.imgTrack)
                    }
                } ?: kotlin.run {
                    binding.includedPlaying.mainLayout.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            SpotifyHelper.getInstance(this@MainActivity).playState.collect { isPaused ->
                isPaused?.let {
                    if (isPaused) {
                        binding.includedPlaying.imgPlayPause.setImageResource(R.drawable.play)
                    } else {
                        binding.includedPlaying.imgPlayPause.setImageResource(R.drawable.pause)
                    }
                }
            }
        }
    }

    private fun setSpotify(playlist: Playlist) {
        playlistModel = playlist
        playlistModel?.spotipyId?.let {
            //askGuruTrack = "spotify:track:$it"
            askGuruTrack = AskGuruTrack(
                title = playlistModel?.songTitle ?: "",
                spotifyID = "spotify:track:$it",
                imageUrl = playlistModel?.artwork ?: "",
                subTitle = playlistModel?.artistName ?: ""
            )
            SpotifyHelper.getInstance(this).setCallback(callback = storyOptionCallBack)
            SpotifyHelper.getInstance(this).authenticateSpotify()
            //Toast.makeText(this, "Playing - ${playlist.songTitle}", Toast.LENGTH_SHORT).show()
        } ?: kotlin.run {
            Toast.makeText(this, "can't play", Toast.LENGTH_SHORT).show()
        }
    }


    private fun openScannerScreen() {
        val intent = Intent(this, ScannerActivity::class.java)
        startActivity(intent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Forward the result to the current fragment in the NavHostFragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
        val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment
        currentFragment?.onActivityResult(requestCode, resultCode, data)
    }

    private val storyOptionCallBack = object : SpotifyCallback {
        override fun onAlreadyAuthenticated() {
            startPlaying()
        }

        override fun onAuthSuccess(accessToken: String) {
            val isConnected = SpotifyHelper.getInstance(this@MainActivity).isConnected()
            if (isConnected) {
                startPlaying()
            } else {
                SpotifyHelper.getInstance(this@MainActivity).connectSpotify()
            }
        }

        override fun onAuthFailure(error: String) {
            Toast.makeText(this@MainActivity, "Auth - $error", Toast.LENGTH_SHORT).show()
        }

        override fun onConnectedSuccess(appRemote: SpotifyAppRemote) {
            startPlaying()
        }

        override fun onConnectedError(error: String) {
            Toast.makeText(this@MainActivity, "Connection - $error", Toast.LENGTH_SHORT).show()
        }

        override fun onPause(track: Track) {
            //Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show()
        }

        override fun onPlay(track: Track) {
            Toast.makeText(this@MainActivity, "onPlay()", Toast.LENGTH_SHORT).show()
        }

        override fun onTrackStatusChange(playerState: PlayerState) {
            /* Log.d(
                 "SpotifyHelper",
                 "Playerdetailview playTrack track isPaused ${playerState.isPaused}"
             )*/
            playerState?.let {
                it.track?.let {
                    if (playerState.isPaused) {
                        binding.includedPlaying.imgPlayPause.setImageResource(R.drawable.play)
                    } else {
                        binding.includedPlaying.imgPlayPause.setImageResource(R.drawable.pause)
                    }
                }
            }
        }

        override fun onPlaybackError(error: String) {
            Toast.makeText(this@MainActivity, "Play Error - $error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPlaying() {
        newPlaying()
    }

    private fun newPlaying() {
        val songList = mutableListOf<AskGuruTrack>()
        askGuruTrack?.let {
            songList.add(it)
        }
        val recommendation = playlistModel?.recommendations?.filter { it.spotipyId != null && it.spotipyId != "" }
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
        SpotifyHelper.getInstance(this).setUpPlayList(songList)
    }
}