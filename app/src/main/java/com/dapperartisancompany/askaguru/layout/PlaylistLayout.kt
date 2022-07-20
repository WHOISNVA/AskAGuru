package com.dapperartisancompany.askaguru

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlayListLayout(){
    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        ) {
            Text(text = "16 tracks, 55 min")
        }
    }
}