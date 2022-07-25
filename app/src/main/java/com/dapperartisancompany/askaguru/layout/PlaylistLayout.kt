package com.dapperartisancompany.askaguru

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.dapperartisancompany.askaguru.data.model.Audio
import java.lang.Math.floor

private val dummyAudioList = listOf(
    Audio(
        uri = "".toUri(),
        displayName = "Kotlin Programming",
        id = 0L,
        artist = "Hood",
        data = "",
        duration = 12345,
        title = "Android Programming"
    ),
    Audio(
        uri = "".toUri(),
        displayName = "Kotlin Programming",
        id = 0L,
        artist = "Lab",
        data = "",
        duration = 25678,
        title = "Android Programming"
    ),
    Audio(
        uri = "".toUri(),
        displayName = "Kotlin Programming",
        id = 0L,
        artist = "Android Lab",
        data = "",
        duration = 8765454,
        title = "Android Programming"
    ),
    Audio(
        uri = "".toUri(),
        displayName = "Kotlin Programming",
        id = 0L,
        artist = "Kotlin Lab",
        data = "",
        duration = 23456,
        title = "Android Programming"
    ),
    Audio(
        uri = "".toUri(),
        displayName = "Kotlin Programming",
        id = 0L,
        artist = "Hood Lab",
        data = "",
        duration = 65788,
        title = "Android Programming"
    ),
    Audio(
        uri = "".toUri(),
        displayName = "Kotlin Programming",
        id = 0L,
        artist = "Hood Lab",
        data = "",
        duration = 234567,
        title = "Android Programming"
    ),

    )

@Composable
fun PlayListLayout(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    isAudioPlaying: Boolean,
    audioList: List<Audio>,
    currentPlayingAudio: Audio?,
    onStart: (Audio) -> Unit,
    onItemClick: (Audio) -> Unit,
    onNext: () -> Unit
){

    LazyColumn(
        contentPadding = PaddingValues(bottom = 56.dp)
    ) {
        items(audioList) { audio: Audio ->
            AudioItem(
                audio = audio,
                onItemClick = { onItemClick.invoke(audio)},
            )
        }
    }
    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .padding(10.dp)
        ,
        ) {
            Text(text = "16 tracks, 55 min" , color = Color.DarkGray)
        }
    }
}

@Composable
fun AudioItem(
    audio: Audio,
    onItemClick: (id: Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onItemClick.invoke(audio.id)
            },
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = .5f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = audio.displayName,
                    style = MaterialTheme.typography.h6,
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = audio.artist,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    color = MaterialTheme.colors
                        .onSurface
                        .copy(alpha = .5f)
                )

            }
            Text(text = timeStampToDuration(audio.duration.toLong()))
            Spacer(modifier = Modifier.size(8.dp))
        }

    }


}

private fun timeStampToDuration(position:Long):String{
    val totalSeconds = floor(position / 1E3).toInt()
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds - (minutes * 60)

    return if (position < 0) "--:--"
    else "%d:%02d".format(minutes,remainingSeconds)



}