package com.ai.image.gen.presentation.i2i

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditScreen(
    onNavigateHome: () -> Unit,
    viewModel: ImageEditViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val selectedUri by viewModel.selectedUri.collectAsStateWithLifecycle()
    val selectedStyle by viewModel.selectedStyle.collectAsStateWithLifecycle()
    val customPrompt by viewModel.customPrompt.collectAsStateWithLifecycle()
    val isEnabled by viewModel.isGenerateEnabled.collectAsStateWithLifecycle()

    var showCustomDialog by remember { mutableStateOf(false) }

    // Permission Launcher for Notifications (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.generateEdit()
                Toast.makeText(context, "Queued! Check notifications.", Toast.LENGTH_LONG).show()
                onNavigateHome()
            } else {
                Toast.makeText(context, "Notifications needed to alert you when ready.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Image Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Image Editor",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showCustomDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Custom Prompt",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 2. Pick Image Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Take available space
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tap to pick an image",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3. Generate Button
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.generateEdit()
                        Toast.makeText(context, "Queued! Check notifications.", Toast.LENGTH_LONG).show()
                        onNavigateHome()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isEnabled,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Magic Edit ✨")
            }

            // 4. Styles Row
            Column {
                Text(
                    text = "Choose Style",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(ImageStyle.values()) { style ->
                        StyleItem(
                            style = style,
                            isSelected = selectedStyle == style,
                            onClick = {
                                viewModel.onStyleSelected(style)
                                if (style == ImageStyle.CUSTOM) showCustomDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomPromptDialog(
            currentPrompt = customPrompt,
            onDismiss = { showCustomDialog = false },
            onConfirm = {
                viewModel.onCustomPromptChanged(it)
                viewModel.onStyleSelected(ImageStyle.CUSTOM)
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun StyleItem(
    style: ImageStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = style.title,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CustomPromptDialog(
    currentPrompt: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentPrompt) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Instruction") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("What to change?") },
                placeholder = { Text("e.g. Make the sky purple") },
                minLines = 3
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(text) }) {
                Text("Set Prompt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}