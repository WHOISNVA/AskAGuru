package com.example.askguru.viewmodel.profile

import com.example.askguru.viewmodel.home.Recommendation
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ProfileDataResponse(
    val playlists: Playlists,
    val user: User
)

data class Playlists(
    @SerializedName("contributions")
    val contributions: List<Contribution> = listOf(),
    val liked_playlists: List<LikedPlaylists>,
    @SerializedName("my_playlists")
    val myPlaylists: List<MyPlaylists> = listOf()
)

data class User(
    val accepted_recs_count: Any,
    val access_token: Any,
    val biography: String,
    val created_on: String,
    val email: String,
    val email_verified: Boolean,
    val follower_data: FollowerData,
    val is_active: Boolean,
    val is_trending: Boolean,
    val profile_pic: String,
    val total_listens: Int,
    val updated_on: String,
    val user_id: String,
    val username: String
)


data class LikedPlaylists(
    val playlist: Playlist
)

data class Contribution(
    @SerializedName("playlist")
    val playlist: Playlist = Playlist()
)

data class MyPlaylists(
    @SerializedName("playlist")
    val playlist: Playlist = Playlist()
)


data class Playlist(
    @SerializedName("playlist_id")
    val playlistId: String = "", // d508b766-bca1-4f70-b7d3-505593c698df
    @SerializedName("owner")
    val owner: String = "", // ca5b5f22-59f0-4b02-9d0f-e3f73c267779
    @SerializedName("song_title")
    val songTitle: String = "", // Relaxin At Club F****n
    @SerializedName("artist_name")
    val artistName: String = "", // Koop
    @SerializedName("song_url")
    val songUrl: String = "", // https://music.apple.com/us/album/relaxin-at-club-f-n/285952445?i=285952709
    @SerializedName("created_on")
    val createdOn: String = "", // 2022-10-24T00:41:34.796137
    @SerializedName("updated_on")
    val updatedOn: String = "", // 2022-10-24T00:41:34.796137
    @SerializedName("artwork")
    val artwork: String = "", // https://is2-ssl.mzstatic.com/image/thumb/Music114/v4/e0/b3/44/e0b34494-4bc8-8c22-cfed-a57b373a659d/mzi.avtzapod.jpg/104x104bb.jpg
    @SerializedName("song_id")
    val songId: String = "", // 285952709
    @SerializedName("genre")
    val genre: String = "", // Electronic
    @SerializedName("listens")
    val listens: Double = 0.0, // 10.0
    @SerializedName("is_trending")
    val isTrending: Boolean = false, // false
    @SerializedName("song_duration")
    val songDuration: Double = 0.0, // 256027.0
    @SerializedName("username")
    val username: String = "", // guru_48624a58
    @SerializedName("recommendations")
    val recommendations: List<Recommendation> = listOf(),
    @SerializedName("playlist_like_count")
    val playlistLikeCount: Double = 0.0, // 1.0
    @SerializedName("spotipy_id")
    val spotipyId: String? = null, // d508b766-bca1-4f70-b7d3-505593c698df
): Serializable

/*data class Recommendation(
    @SerializedName("playlist_id")
    val playlistId: String = "", // d508b766-bca1-4f70-b7d3-505593c698df
    @SerializedName("song_title")
    val songTitle: String = "", // Rose Rouge
    @SerializedName("artist_name")
    val artistName: String = "", // St Germain
    @SerializedName("song_url")
    val songUrl: String = "", // https://music.apple.com/us/album/rose-rouge/717406560?i=717406613
    @SerializedName("created_on")
    val createdOn: String = "", // 2023-04-21T04:10:16.547299
    @SerializedName("updated_on")
    val updatedOn: String = "", // 2023-04-21T04:10:16.547299
    @SerializedName("artwork")
    val artwork: String = "", // https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/93/40/66/93406699-6ea0-544a-1842-4ec070a83b99/5099991455758.jpg/48x48bb.jpg
    @SerializedName("accepted_by")
    val acceptedBy: String = "", // ca5b5f22-59f0-4b02-9d0f-e3f73c267779
    @SerializedName("song_id")
    val songId: String = "", // 717406613
    @SerializedName("song_duration")
    val songDuration: Double = 0.0, // 416557.0
    @SerializedName("recommendation_id")
    val recommendationId: String = "", // a78a5838-aafb-48f9-a41f-3938dbfd3a25
    @SerializedName("recommender_id")
    val recommenderId: String = "", // ca5b5f22-59f0-4b02-9d0f-e3f73c267779
    @SerializedName("accepted")
    val accepted: Boolean = false, // true
    @SerializedName("owner")
    val owner: String = "", // ca5b5f22-59f0-4b02-9d0f-e3f73c267779
    @SerializedName("username")
    val username: String = "" // guru_48624a58
)*/

data class FollowerData(
    val follower_count: Int,
    val followers: List<Followers>,
    //val following: List<Any>,
    //val following_count: Int
)

data class Followers(
    @SerializedName("followed_id")
    val followed_id: String = ""
)