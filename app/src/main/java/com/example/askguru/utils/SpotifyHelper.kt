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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.*

class SpotifyHelper(private val context: Context) {
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var isAuthenticated = false
    private var spotifyCallback: SpotifyCallback? = null
    var isPaused: Boolean = false
    var trackWasStarted: Boolean = false

    private val playerStateData = MutableStateFlow<PlayerState?>(null)

    private var recommendationQueueList = mutableListOf<String>()
    private var myTrack: String = ""
    private val TAG = "SpotifyHelper"

    val stringQueue = LinkedList<String>()

    // Add data to the queue
    fun enqueueData(data: String) {
        stringQueue.add(data)
    }

    // Get data from the front of the queue
    fun dequeueData(): String? {
        return if (stringQueue.isNotEmpty()) {
            stringQueue.removeFirst()
        } else {
            null
        }
    }

    fun setCallback(callback: SpotifyCallback) {
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

    private var mySongList = listOf<String>()
    private var currentPlayingIndex = 0
    fun setUpPlayList(songList: MutableList<String>) {
        Log.d(TAG, "new :: setUpPlayList $songList")
        updatePlayerData()
        mySongList = songList
        currentPlayingIndex = 0
        playCurrentTrack()
        //playNewSong(mySongList.first())
        spotifyAppRemote?.let {
            it.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
                isPaused = playerState.isPaused
                playerStateData.value = playerState
                spotifyCallback?.onTrackStatusChange(playerState)
                // jumpToNextSongIfNeeded(currentTime = it.playbackPosition, duration = track.duration)*/
            }

            it.playerApi.playerState.setErrorCallback { error ->
                spotifyCallback?.onPlaybackError("${error.message}")
                Log.d(TAG, "new2 :: setErrorCallback ${error.message}")
            }
        }
    }

    private fun updatePlayerData() {
        GlobalScope.launch {
            playerStateData
                .debounce(500)
                .collect { playerState ->
                    val log = StringBuilder()
                    playerState?.let {
                        val track: Track? = playerState.track
                        track?.let {
                            log.append("current track url :: ${track.uri} & currentDuration :: ${playerState.playbackPosition} of total ${track.duration} :: isPaused ${playerState.isPaused}")
                            setTrackWasStarted(playerState) // this func from paulgoetze above

                            val isPaused = playerState.isPaused
                            val position = playerState.playbackPosition
                            val hasEnded = trackWasStarted && isPaused && position == 0L
                            //val songPlayingIsNotRight = it.track?.uri != myTrack/* expectedSong.uri or null if you want to stop */
                            val songPlayingIsNotRight =
                                !isPlayingCorrectSong2(playerState.track?.uri)/* expectedSong.uri or null if you want to stop */

                            log.append(" trackWasStarted:$trackWasStarted hasEnded:$hasEnded songPlayingIsNotRight:$songPlayingIsNotRight")

                            if (hasEnded) {
                                trackWasStarted = false
                                nextTrack()
                                log.append(" if :: new song $myTrack songPos : $currentPlayingIndex")
                            } else if (songPlayingIsNotRight && !playerState.isPaused) {
                                nextTrack()
                                log.append(" else if :: new song $myTrack songPos : $currentPlayingIndex")
                            } else {
                                log.append(" else")
                            }

                        } ?: kotlin.run {
                            log.append("track is null")
                        }
                    } ?: kotlin.run {
                        log.append("playerState is null")
                    }
                    Log.d(TAG, "new2 :: subscribeToPlayerState $log")
                }
        }
    }

    private fun nextTrack() {
        Log.d(TAG, "new :: nextTrack")
        currentPlayingIndex?.let { index ->
            when {
                index >= mySongList.size - 1 -> {
                    pause()
                }
                else -> {
                    currentPlayingIndex = index + 1
                    playCurrentTrack()
                }
            }
        }
    }

    private fun playCurrentTrack() {
        if (mySongList.isEmpty()) {
            return
        }

        val currentTrackUri = mySongList[currentPlayingIndex]
        Log.d(TAG, "new :: currentTrackUri $currentTrackUri")
        myTrack = currentTrackUri
        spotifyAppRemote?.playerApi?.play(currentTrackUri)
    }

    private fun isPlayingCorrectSong2(currentPlayingTrack: String?): Boolean {
        //if (currentPlayingTrack == myTrack || recommendationQueueList.contains(currentPlayingTrack)) {
        if (currentPlayingTrack == myTrack || mySongList.contains(currentPlayingTrack)) {
            //Log.d(TAG,"isPlayingCorrectSong true $currentPlayingTrack myTrack $myTrack recommendationQueueList $recommendationQueueList")
            return true
        }
        //Log.d(TAG,"isPlayingCorrectSong false $currentPlayingTrack myTrack $myTrack recommendationQueueList $recommendationQueueList")
        return false
    }

    fun playRecommendationListClick(spotifyID: String) {
        Log.d(TAG, "new2 :: playing playRecommendationListClick $spotifyID")
        currentPlayingIndex = mySongList.indexOf(spotifyID)
        playCurrentTrack()
    }

    // ...
    private fun setTrackWasStarted(playerState: PlayerState) {
        val position = playerState.playbackPosition
        val duration = playerState.track.duration
        val isPlaying = !playerState.isPaused

        if (!trackWasStarted && position > 0 && duration > 0 && isPlaying) {
            trackWasStarted = true
        }
    }

    fun clearQueue() {
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

    fun stop() {
        spotifyAppRemote?.playerApi?.pause()
    }

    fun authenticateSpotify() {
        if (isAuthenticated) {
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
