package com.dapperartisancompany.askaguru.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dapperartisancompany.askaguru.R

@Composable
@Preview
fun Header(){
    Row( modifier = Modifier
        .background(Color.Black)
        .fillMaxWidth()
        .height(100.dp)
        .padding(10.dp, 10.dp, 10.dp, 10.dp),
        Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        )
    {
        Column(Modifier
            .padding(0.dp, 0.dp, 0.dp,0.dp)
            .align(Alignment.Top)) {
            Text(text = "Login/Register", color = Color.White, fontSize = 11.sp,
            )
        }
        Column(modifier = Modifier.padding(0.dp,0.dp,0.dp,0.dp)) {
            Image(
                painter = painterResource(id = R.drawable.askaguru_logo),
                contentDescription = "Ask A Guru Logo",
                modifier = Modifier
                    .width(90.dp)
                    .height(81.dp)
                    .padding(10.dp, 10.dp, 10.dp, 10.dp)
            )
        }
        Column(Modifier.align(Alignment.Top)) {
            Image(modifier = Modifier
                .height(45.dp)
                .width(45.dp),
                painter = painterResource(id = R.drawable.ic_search_foreground),
                contentDescription = "")
        }

    }
}