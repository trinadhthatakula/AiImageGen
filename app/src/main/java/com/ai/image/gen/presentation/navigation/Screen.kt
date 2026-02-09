package com.ai.image.gen.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.ai.image.gen.presentation.vectors.PhotoLibrary
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Sealed class defines the topology of the app.
 * TopLevel screens appear in the Bottom Bar.
 * Leaf screens (like T2I) are hidden from the bar.
 */
sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {

    // Bottom Nav Items
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Saved : Screen("saved", "Gallery", PhotoLibrary)

    // Leaf Screens (Features)
    data object TextToImage : Screen("t2i")
    data object ImageEdit : Screen("image_edit")

    data object EditResult : Screen("edit_result/{encodedPath}") {
        fun createRoute(filePath: String): String {
            // CRITICAL: File paths contain '/', which breaks Compose Navigation.
            // We MUST URL-encode the path before passing it.
            val encoded = URLEncoder.encode(filePath, StandardCharsets.UTF_8.toString())
            return "edit_result/$encoded"
        }
    }
}