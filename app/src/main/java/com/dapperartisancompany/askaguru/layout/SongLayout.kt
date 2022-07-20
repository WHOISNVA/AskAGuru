package com.dapperartisancompany.askaguru.layout

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dapperartisancompany.askaguru.data.model.Song

@Composable
fun SongItem(song: Song?, listener: () -> Unit = {}) {
    if (song != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { listener() })
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongArt(
                song.art,
                Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(50.dp)
            )
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    song.name,
                    style = TextStyle(
                        color = MaterialTheme.colors.background,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = song.artist,
                    style = TextStyle(
                        color = MaterialTheme.colors.background,
                        fontSize = 14.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SongArt(art: ImageBitmap?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
        if (art != null) {
            Image(
                art,
                contentDescription = null,
                modifier = Modifier.wrapContentSize(Alignment.Center)
            )
        } else {
            Image(
                Icons.Default.Star,
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.background),
                modifier = Modifier.wrapContentSize(Alignment.Center)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SongScreenPreview() {

}
