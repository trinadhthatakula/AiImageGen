package com.ai.image.gen

import android.R.attr.height
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ai.image.gen.presentation.navigation.AppNavigation
import com.ai.image.gen.presentation.theme.AIImageGenTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpSplash(installSplashScreen())
        enableEdgeToEdge()
        setContent {
            AIImageGenTheme {
                AppNavigation()
            }
        }
    }

    private fun setUpSplash(splashScreen: SplashScreen) {
        var keepSplashOnScreen = true
        val delayTime = 800L // 600ms animation + 200ms buffer
        // This is a simplified "hold" mechanism.
        // In Valhalla production code, you'd use a ViewModel state.
        // We run a background timer to release the block.
        Thread {
            Thread.sleep(delayTime)
            keepSplashOnScreen = false
        }.start()
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }
        // 2. Optional: Add a custom Exit Animation (Zoom out the icon)
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView.iconView,
                View.TRANSLATION_Y,
                0f,
                -splashScreenView.view.height.toFloat()
            )
            slideUp.interpolator = AnticipateInterpolator()
            slideUp.duration = 400L
            // Call remove() when animation finishes
            slideUp.doOnEnd { splashScreenView.remove() }
            slideUp.start()
        }
    }

}

