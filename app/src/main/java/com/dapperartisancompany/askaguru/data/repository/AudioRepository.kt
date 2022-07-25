package com.dapperartisancompany.askaguru.data.repository

import com.dapperartisancompany.askaguru.ContentResolverHelper
import com.dapperartisancompany.askaguru.data.model.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class AudioRepository @Inject
constructor(private val contentResolverHelper: ContentResolverHelper) {
    suspend fun getAudioData():List<Audio> = withContext(Dispatchers.IO){
        contentResolverHelper.getAudioData()
    }
}