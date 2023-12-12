package com.example.askguru.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.askguru.R
import com.example.askguru.databinding.ActivityMainBinding
import com.example.askguru.ui.login.LoginActivity
import com.example.askguru.ui.qr.ScannerActivity
import com.example.askguru.utils.Const.Companion.PRE_IS_LOGIN
import com.example.askguru.utils.PreferenceHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.permissionx.guolindev.PermissionX


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    }

    private fun openScannerScreen() {
        val intent = Intent(this, ScannerActivity::class.java)
        startActivity(intent)
    }

    /*private var spotifyAppRemote: SpotifyAppRemote? = null
    override fun onStart() {
        super.onStart()
    //    spotifyConnection()
        spotifyAuth()
    }

    private fun spotifyConnection(){
        val connectionParams = ConnectionParams.Builder(Const.SPOTIPY_CLIENT_ID)
            .setRedirectUri(SPOTIPY_REDIRECT_URL)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                Log.e("MainActivity", "spotifyConnection Connected! Yay!")
                // Now you can start interacting with App Remote
                connected()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("MainActivity", "spotifyConnection ${throwable.message}")
                // Something went wrong when attempting to connect! Handle errors here
            }
        })
    }


    private fun connected() {
        spotifyAppRemote?.let {
            // Play a playlist
            val playlistURI = "spotify:playlist:30605fce-a75c-4e7c-af3f-75348b73fabe"
            //val playlistURI = "spotify:track:2bgcUk2A3jjKbCJ7KPquTi"

            it.playerApi.play(playlistURI)
            // Subscribe to PlayerState
            it.playerApi.subscribeToPlayerState().setEventCallback {
                Log.d("MainActivity","spotifyConnection spotifyAppRemote isPaused ${it.isPaused}")
                val track: Track = it.track
                Log.d("MainActivity", "spotifyConnection" + track.name + " by " + track.artist.name)
            }
        }

        spotifyAppRemote?.playerApi?.play("spotify:track:6SEolIp22t0DzeBfCBo3hr")

    }

    private fun pause(){
        spotifyAppRemote?.playerApi?.pause()
    }

    private fun resume(){
        spotifyAppRemote?.playerApi?.resume()
    }


    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }

    private fun spotifyAuth(){
        var REDIRECT_URI = SPOTIPY_REDIRECT_URL

        val builder = AuthorizationRequest.Builder(Const.SPOTIPY_CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
        builder.setScopes(arrayOf("streaming","user-read-email"))
        //builder.setCampaign("your-campaign-token")
        val request = builder.build()

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)

            Toast.makeText(this@MainActivity, "response :: ${response.type}", Toast.LENGTH_SHORT).show()
            Log.e("MainActivity", "onActivityResult response ${response.error}")
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    Log.e("MainActivity", "onActivityResult response ${response.accessToken}")
                 spotifyConnection()
                }
                AuthorizationResponse.Type.ERROR -> {
                    Toast.makeText(this@MainActivity, "response :: ${response.error}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }*/

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        SpotifyHelper.getInstance(this).handleSpotifyAuthResponse(requestCode, resultCode, intent)
    }


    //private var playTrackID = "spotify:track:6SEolIp22t0DzeBfCBo3hr"//default
    //private var playTrackID = "spotify:track:2bgcUk2A3jjKbCJ7KPquTi"//Mi Niña
    //private var playTrackID = "spotify:track:0hnvQLkV7yOjslEzflJSK4"//La Curiosidad
    private var playTrackID = "spotify:track:1xK1Gg9SxG8fy2Ya373oqb"//Bandido

    private val storyOptionCallBack = object : SpotifyCallback {
        override fun onAlreadyAuthenticated() {
        }

        override fun onAuthSuccess(accessToken: String) {
            val isConnected = SpotifyHelper.getInstance(this@MainActivity).isConnected()
            if(isConnected){
                SpotifyHelper.getInstance(this@MainActivity).playTrack(playTrackID)
            }else{
               SpotifyHelper.getInstance(this@MainActivity).connectSpotify()
            }
        }

        override fun onAuthFailure(error: String) {

        }

        override fun onConnectedSuccess(appRemote: SpotifyAppRemote) {
            SpotifyHelper.getInstance(this@MainActivity).playTrack(playTrackID)
        }

        override fun onConnectedError(error: String) {
        }
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Forward the result to the current fragment in the NavHostFragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
        val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment
        currentFragment?.onActivityResult(requestCode, resultCode, data)
    }
}