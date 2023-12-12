package com.example.askguru.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.SpotifyAppRemote.connect
import com.spotify.android.appremote.api.SpotifyAppRemote.disconnect
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

class SpotifyHelper(private val context: Context) {
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var isAuthenticated = false
    private var spotifyCallback : SpotifyCallback? = null
    var isPaused : Boolean = false

    private var recommendationQueueList =  mutableListOf<String>()
    private var myTrack : String = ""
    private val TAG = "SpotifyHelper"

    fun setCallback(callback: SpotifyCallback){
        spotifyCallback = callback
    }

    fun connectSpotify() {
        Log.d(TAG, "connectSpotify started")
        val connectionParams = ConnectionParams.Builder(Const.SPOTIPY_CLIENT_ID)
            .setRedirectUri(Const.SPOTIPY_REDIRECT_URL)
            .showAuthView(true)
            .build()

        connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                Log.d(TAG, "connectSpotify onConnected")
                spotifyAppRemote = appRemote
                spotifyCallback?.onConnectedSuccess(appRemote)
            }

            override fun onFailure(throwable: Throwable) {
                Log.d(TAG, "connectSpotify onFailure ${throwable.message}")
            }
        })
    }

    fun isConnected(): Boolean {
        return spotifyAppRemote?.isConnected == true
    }

    fun disconnectSpotify() {
        spotifyAppRemote?.let {
            disconnect(it)
        }
    }

    fun playTrack(trackUri: String) {
        Log.d(TAG, "playTrack $trackUri")
        myTrack = trackUri
        clearQueue()
        spotifyAppRemote?.let {
            it.playerApi.play(trackUri)
            it.playerApi.subscribeToPlayerState().setEventCallback {
                isPaused = it.isPaused
                //Log.d(TAG,"playTrack track isPaused ${it.isPaused}")
                val track: Track = it.track
                Log.d(TAG, "playTrack Track url ${track.uri}")
                //Log.d(TAG, "playTrack" + track.name + " by " + track.artist.name)
                spotifyCallback?.onTrackStatusChange(it)

                //stop the song if no recommendation
                val trackId = track.uri.toString()
                if (it.playbackSpeed == 0.0f && !isPaused && recommendationQueueList.isEmpty() && trackId != myTrack) {
                    stop()
                    Log.d(TAG,"stop playing as no recommendation is there")
                }else{
                    Log.d(TAG,"not stopping the song, speed - ${it.playbackSpeed}, recommendation size ${recommendationQueueList.size} mytrackID : $myTrack")
                }
            }

            it.playerApi.playerState.setErrorCallback { error ->
                spotifyCallback?.onPlaybackError("${error.message}")
            }

        }
    }

    fun queueSongs(trackList: List<String>){
        trackList.forEach {item->
            val song = "spotify:track:$item"
            //val song = item
            spotifyAppRemote?.playerApi?.queue(song)
            Log.d(TAG, "playTrack added to Queue list :: $song")
        }
        recommendationQueueList = trackList.toMutableList()
    }

    fun clearQueue(){
        recommendationQueueList.clear()
    }

    fun pause() {
        Log.d(TAG, "pause ${spotifyAppRemote?.playerApi?.playerState?.requestId}")
        spotifyAppRemote?.playerApi?.pause()
    }

    fun resume() {
        Log.d(TAG, "resume ${spotifyAppRemote?.playerApi?.playerState?.requestId}")
        spotifyAppRemote?.playerApi?.resume()
    }

    fun stop(){
        spotifyAppRemote?.playerApi?.pause()
    }

    fun authenticateSpotify() {
        if(isAuthenticated){
            Log.d(TAG, "authenticateSpotify already authenticated")
            spotifyCallback?.onAlreadyAuthenticated()
            return
        }

        Log.d(TAG, "authenticateSpotify started")
        val builder = AuthorizationRequest.Builder(
            Const.SPOTIPY_CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            Const.SPOTIPY_REDIRECT_URL
        )
        builder.setScopes(arrayOf("streaming", "user-read-email"))
        val request = builder.build()
        AuthorizationClient.openLoginActivity(context as Activity, AUTH_REQUEST_CODE, request)
    }

    fun handleSpotifyAuthResponse(requestCode: Int, resultCode: Int, intent: Intent?) {
        Log.d(TAG, "authenticateSpotify handleSpotifyAuthResponse")
        if (requestCode == AUTH_REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)

            //Toast.makeText(context, "response :: ${response.type}", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "authenticateSpotify handleSpotifyAuthResponse ${response.type}")
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {

                    Log.d(
                        TAG,
                        "authenticateSpotify handleSpotifyAuthResponse TOKEN ${response.accessToken}"
                    )
                    isAuthenticated = true
                    spotifyCallback?.onAuthSuccess(response.accessToken)
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.d(
                        TAG,
                        "authenticateSpotify handleSpotifyAuthResponse ERROR ${response.error}"
                    )
                    isAuthenticated = false
                    spotifyCallback?.onAuthFailure(response.error)
                }
                else -> {
                    isAuthenticated = false
                }
            }
        }
    }

    companion object {
        const val AUTH_REQUEST_CODE = 101
        private var instance: SpotifyHelper? = null

        @Synchronized
        fun getInstance(context: Context): SpotifyHelper {
            if (instance == null) {
                instance = SpotifyHelper(context)
            }
            return instance!!
        }
    }
}

interface SpotifyCallback {
    fun onAlreadyAuthenticated()
    fun onAuthSuccess(accessToken: String)
    fun onAuthFailure(error: String)
    fun onConnectedSuccess(appRemote: SpotifyAppRemote)
    fun onConnectedError(error: String)
    fun onPause(track: Track)
    fun onPlay(track: Track)
    fun onTrackStatusChange(playerState: PlayerState)
    fun onPlaybackError(error: String)
}
