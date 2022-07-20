package com.dapperartisancompany.askaguru

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dapperartisancompany.askaguru.ui.theme.AskAGuruTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AskAGuruTheme {
                    MainLayout()
                    }
                }
            }
        }