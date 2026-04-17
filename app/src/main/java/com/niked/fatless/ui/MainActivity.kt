package com.niked.fatless.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.niked.fatless.ui.navigation.FatLessNavGraph
import com.niked.fatless.ui.theme.FatLessTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FatLessTheme {
                FatLessNavGraph()
            }
        }
    }
}