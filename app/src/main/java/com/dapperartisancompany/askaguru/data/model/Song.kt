package com.dapperartisancompany.askaguru.data.model

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap

data class Song(
    val uri: Uri,
    val name: String,
    val artist: String,
    val duration: Int,
    val art: ImageBitmap? = null
)