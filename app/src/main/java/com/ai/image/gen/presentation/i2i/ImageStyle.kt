package com.ai.image.gen.presentation.i2i

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.graphics.vector.ImageVector

enum class ImageStyle(
    val title: String,
    val instruction: String,
    val icon: ImageVector
) {
    ANIME("Anime", "Turn this into a high quality anime style illustration", Icons.Default.Face),
    CYBERPUNK("Cyberpunk", "Turn this into a cyberpunk style with neon lights", Icons.Default.Movie),
    SKETCH("Pencil Sketch", "Turn this into a detailed pencil sketch", Icons.Default.Brush),
    OIL_PAINTING("Oil Painting", "Turn this into an oil painting by Van Gogh", Icons.Default.Palette),
    FANTASY("Fantasy", "Turn this into a fantasy concept art", Icons.Default.Landscape),
    // Special case for custom prompt
    CUSTOM("Custom", "", Icons.Default.Brush)
}