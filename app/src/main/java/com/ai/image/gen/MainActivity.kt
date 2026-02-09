package com.ai.image.gen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ai.image.gen.presentation.navigation.AppNavigation
import com.ai.image.gen.presentation.theme.AIImageGenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIImageGenTheme {
                AppNavigation()
            }
        }
    }
}

