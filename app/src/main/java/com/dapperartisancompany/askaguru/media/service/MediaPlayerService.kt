package com.dapperartisancompany.askaguru.media.service

import android.os.Bundle

import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import com.dapperartisancompany.askaguru.media.constants.K
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaPlayerService:MediaBrowserServiceCompat() {

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(K.MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }
}