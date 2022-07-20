package com.dapperartisancompany.askaguru

import android.content.Context
import android.database.Cursor
import android.hardware.biometrics.BiometricManager
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ContentResolverHelper @Inject constructor(@ApplicationContext val context: Context) {
    private var mCursor: Cursor? = null

    private val projection: Array<String> = arrayOf(
        MediaStore.Audio.AudioColumns.DISPLAY_NAME,
        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.AudioColumns.DURATION,
        MediaStore.Audio.AudioColumns.TITLE,
    )

    private var selectionClause: String? = "${MediaStore.Audio.AudioColumns.IS_MUSIC} = ?"

    private var selectionArg = arrayOf("1")

    private var sortOrder = "${MediaStore.Audio.AudioColumns.DISPLAY_NAME} ASC"



}