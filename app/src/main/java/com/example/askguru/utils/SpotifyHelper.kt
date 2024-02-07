package com.example.askguru.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SpotifyHelper(private val context: Activity) {
    private val TAG = "SpotifyHelper"

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var isAuthenticated = false
    private var spotifyCallback: SpotifyCallback? = null
    private var trackWasStarted: Boolean = false
    private var myTrack: String = ""
    private val playerStateData = MutableStateFlow<PlayerState?>(null)

    private var mySongList = mutableListOf<AskGuruTrack>()
    private var currentPlayingIndex = 0
    private var liveRunningTrack : String = ""

    var isPaused: Boolean = false
    val currentAskGuruTrack = MutableStateFlow<AskGuruTrack?>(null)
    val playState = MutableStateFlow<Boolean?>(null)

    private var collectorJob: Job? = null

    fun setCallback(callback: SpotifyCallback) {
        spotifyCallback = callback
    }

    fun authenticateSpotify() {
        if (isAuthenticated) {
            logD("authenticateSpotify already authenticated")
            spotifyCallback?.onAlreadyAuthenticated()
            return
        }

        logD("authenticateSpotify started")
        val builder = AuthorizationRequest.Builder(
            Const.SPOTIPY_CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            Const.SPOTIPY_REDIRECT_URL
        )
        builder.setScopes(arrayOf("streaming", "user-read-email"))
        val request = builder.build()
        //AuthorizationClient.openLoginActivity(context, AUTH_REQUEST_CODE, request)
        try {
            val intent = AuthorizationClient.createLoginActivityIntent(context, request)
            context.startActivityForResult(intent, AUTH_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
            logD("Exception during login activity start: ${e.message}")
        }
    }

    fun handleSpotifyAuthResponse(requestCode: Int, resultCode: Int, intent: Intent?) {
        logD("authenticateSpotify handleSpotifyAuthResponse")
        if (requestCode == AUTH_REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)

            //Toast.makeText(context, "response :: ${response.type}", Toast.LENGTH_SHORT).show()
            logD("authenticateSpotify handleSpotifyAuthResponse ${response.type}")
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {

                    logD(
                        "authenticateSpotify handleSpotifyAuthResponse TOKEN ${response.accessToken}"
                    )
                    isAuthenticated = true
                    spotifyCallback?.onAuthSuccess(response.accessToken)
                }
                AuthorizationResponse.Type.ERROR -> {
                    logD(
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

    fun isConnected(): Boolean {
        return spotifyAppRemote?.isConnected == true
    }

    fun connectSpotify() {
        logD("connectSpotify started")
        val connectionParams = ConnectionParams.Builder(Const.SPOTIPY_CLIENT_ID)
            .setRedirectUri(Const.SPOTIPY_REDIRECT_URL)
            .showAuthView(true)
            .build()

        connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                logD("connectSpotify onConnected")
                spotifyAppRemote = appRemote
                spotifyCallback?.onConnectedSuccess(appRemote)
            }

            override fun onFailure(throwable: Throwable) {
                logD("connectSpotify onFailure ${throwable.message}")
            }
        })
    }

    fun setUpPlayList(songList: MutableList<AskGuruTrack>) {
        resetValues()
        logD("new :: setUpPlayList $songList")
        updatePlayerData()
        mySongList.clear()
        mySongList.addAll(songList)
        currentPlayingIndex = 0
        playCurrentTrack()
        spotifyAppRemote?.let {
            it.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
                liveRunningTrack = playerState.track.uri.toString()
                isPaused = playerState.isPaused
                playerStateData.value = playerState
                playState.value = playerState.isPaused
                spotifyCallback?.onTrackStatusChange(playerState)
            }

            it.playerApi.playerState.setErrorCallback { error ->
                spotifyCallback?.onPlaybackError("${error.message}")
                logD("new :: setErrorCallback ${error.message}")
            }
        }
    }

    private fun updatePlayerData() {
        collectorJob?.cancel()
        collectorJob = GlobalScope.launch {
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
                            //val songPlayingIsNotRight = !isPlayingCorrectSong(track.uri)/* expectedSong.uri or null if you want to stop */
                            val songPlayingIsNotRight = !isPlayingCorrectSong(liveRunningTrack)/* expectedSong.uri or null if you want to stop */

                            log.append(" trackWasStarted:$trackWasStarted hasEnded:$hasEnded songPlayingIsNotRight:$songPlayingIsNotRight")

                            if (hasEnded) {
                                trackWasStarted = false
                                nextTrack("hasEnded")
                                log.append(" if :: new song $myTrack songPos : $currentPlayingIndex")
                            } else if (songPlayingIsNotRight && !playerState.isPaused) {
                                nextTrack("else if :: playing track $liveRunningTrack")
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
                    logD("new :: subscribeToPlayerState $log")
                }
        }
    }

    private fun resetValues(){
        mySongList.clear()
        myTrack = ""
        trackWasStarted = false
        isPaused = false
        currentPlayingIndex = 0
        liveRunningTrack = ""
    }

    private fun nextTrack(strData : String = "default") {
        logD("new :: nextTrack :: $strData")
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
        val askGuruTrack = mySongList[currentPlayingIndex]
        spotifyAppRemote?.playerApi?.play(askGuruTrack.spotifyID)
        myTrack = askGuruTrack.spotifyID
        logD("new :: currentTrackUri $myTrack")
        currentAskGuruTrack.value = askGuruTrack
    }

    private fun isPlayingCorrectSong(currentPlayingTrack: String?): Boolean {
        //if (currentPlayingTrack == myTrack || recommendationQueueList.contains(currentPlayingTrack)) {
        //if (currentPlayingTrack == myTrack || mySongList.contains(currentPlayingTrack)) {
        if (currentPlayingTrack == myTrack || mySongList.any { it.spotifyID == currentPlayingTrack }) {
            //Log.d(TAG,"isPlayingCorrectSong true $currentPlayingTrack myTrack $myTrack recommendationQueueList $recommendationQueueList")
            return true
        }
        //Log.d(TAG,"isPlayingCorrectSong false $currentPlayingTrack myTrack $myTrack recommendationQueueList $recommendationQueueList")
        return false
    }

    fun playNext(){
        logD("new :: playNext")
        currentPlayingIndex?.let { index ->
            when {
                index >= mySongList.size - 1 -> {
                    //pause()
                    Toast.makeText(context, "Last song", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    currentPlayingIndex = index + 1
                    playCurrentTrack()
                }
            }
        }
    }

    fun playPrevious(){
        logD("new :: playPrevious")
        currentPlayingIndex?.let { index ->
            when {
                index > 0 -> {
                    currentPlayingIndex = index - 1
                    playCurrentTrack()
                }
                else -> {
                    Toast.makeText(context, "First song", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun playRecommendationListClick(spotifyID: String) {
        logD("new :: playing playRecommendationListClick $spotifyID")
        //currentPlayingIndex = mySongList.indexOf(spotifyID)
        currentPlayingIndex = mySongList.indexOfFirst { it.spotifyID == spotifyID }
        playCurrentTrack()
    }

    private fun setTrackWasStarted(playerState: PlayerState) {
        val position = playerState.playbackPosition
        val duration = playerState.track.duration
        val isPlaying = !playerState.isPaused

        if (!trackWasStarted && position > 0 && duration > 0 && isPlaying) {
            trackWasStarted = true
        }
    }

    fun pause() {
        logD("pause ${spotifyAppRemote?.playerApi?.playerState?.requestId}")
        spotifyAppRemote?.playerApi?.pause()
    }

    fun resume() {
        logD("resume ${spotifyAppRemote?.playerApi?.playerState?.requestId}")
        spotifyAppRemote?.playerApi?.resume()
    }

    fun stop() {
        spotifyAppRemote?.playerApi?.pause()
    }

    private fun disconnectSpotify() {
        spotifyAppRemote?.let {
            disconnect(it)
        }
    }

   private fun revokeAuthentication() {
        AuthorizationClient.clearCookies(context)
    }

    fun logout() {
        pause()

        revokeAuthentication()

        // Disconnect Spotify
        disconnectSpotify()

        // Reset values
        resetValues()

        // Reset authentication status
        isAuthenticated = false

        instance = null
    }

    @SuppressLint("LogNotTimber")
    fun logD(strLog: String) {
        Log.d(TAG,strLog)
    }

    companion object {
        const val AUTH_REQUEST_CODE = 101

        @SuppressLint("StaticFieldLeak")
        private var instance: SpotifyHelper? = null

        @Synchronized
        fun getInstance(context: Activity): SpotifyHelper {
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


data class AskGuruTrack(
    val title: String,
    val spotifyID: String,
    val imageUrl: String,
    val subTitle: String
)