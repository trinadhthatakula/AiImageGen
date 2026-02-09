package com.ai.image.gen.presentation.saved

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun SavedImagesScreen(
    onImageClick: (String) -> Unit,
    viewModel: SavedImagesViewModel = koinViewModel()
) {
    val images by viewModel.images.collectAsStateWithLifecycle()

    // Refresh list when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadImages()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Your Gallery",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (images.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ImageNotSupported,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No saved images yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(images, key = { it.absolutePath }) { file ->
                    GalleryItem(file = file, onClick = { onImageClick(file.absolutePath) })
                }
            }
        }
    }
}

@Composable
fun GalleryItem(file: File, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Image(
            painter = rememberAsyncImagePainter(file),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}