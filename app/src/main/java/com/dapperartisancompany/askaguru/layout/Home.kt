package com.dapperartisancompany.askaguru.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dapperartisancompany.askaguru.viewmodels.MainViewModel
import com.dapperartisancompany.askaguru.viewmodels.PlayerSwipeableState
import com.dapperartisancompany.askaguru.viewmodels.PlayerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalCoroutinesApi
@Composable
fun HomeScreen(mainViewModel: MainViewModel, playerViewModel: PlayerViewModel) {
    val mainViewModelState by mainViewModel.state.collectAsState()
    val playerViewModelState by playerViewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    @Composable
    fun CustomChip(
        selected: Boolean,
        text: String,
        modifier: Modifier = Modifier

    ) {
        // define properties to the chip
        // such as color, shape, width
        Surface(
            color = when {
                selected -> MaterialTheme.colors.onSurface
                else -> Color.Transparent
            },
            contentColor = when {
                selected -> MaterialTheme.colors.onPrimary
                else -> Color.White
            },
            shape = CircleShape,
            border = BorderStroke(
                width = 1.dp,
                color = when {
                    selected -> MaterialTheme.colors.onPrimary
                    else -> Color.White
                }
            ),
            modifier = modifier
        ) {
            // Add text to show the data that we passed
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(8.dp)
            )

        }
    }
    Column {
        Row(modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceBetween) {
            // creates a custom chip for active state
            CustomChip(
                selected = true,
                text = "Pop",
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            // Creates a custom chip for inactive state
            CustomChip(
                selected = false,
                text = "Hip-Hop",
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            // Create a custom image chip whose state is active
            CustomChip(
                selected = false,
                text = "Jazz",
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            // Create a custom image chip whose state is inactive
            CustomChip(
                selected = false,
                text = "Rock",
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            CustomChip(
                selected = false,
                text = "Alternative",
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .background(Color.Black),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(items = mainViewModelState.songs) {
                SongItem(it) {
                    playerViewModel.state.value = playerViewModel.state.value.copy(playing = it)
                    coroutineScope.launch {
                        playerViewModelState.playerSwipeState.animateTo(PlayerSwipeableState.Peek)
                    }
                }
            }
        }
    }
}
