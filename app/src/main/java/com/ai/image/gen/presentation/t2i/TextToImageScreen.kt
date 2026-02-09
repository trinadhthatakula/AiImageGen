package com.ai.image.gen.presentation.t2i

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ai.image.gen.domain.ImageGenerationResult
import com.ai.image.gen.presentation.t2i.components.ErrorBox
import com.ai.image.gen.presentation.t2i.components.LoadingBox
import com.ai.image.gen.presentation.t2i.components.PlaceholderBox
import com.ai.image.gen.presentation.t2i.components.ResultImage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextToImageScreen(
    onBackClick: () -> Unit,
    viewModel: ImageViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var prompt by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text to Image") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize().imePadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Section
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Describe your imagination") },
                placeholder = { Text("e.g. Cyberpunk street in Tokyo, rainy night") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = state !is ImageGenerationResult.Loading
            )

            Button(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    viewModel.generateImage(prompt)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = prompt.isNotBlank() && state !is ImageGenerationResult.Loading
            ) {
                Text(if (state is ImageGenerationResult.Loading) "Dreaming..." else "Generate")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Result Section
            AnimatedContent(
                targetState = state,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ImageStateTransition"
            ) { targetState ->
                when (targetState) {
                    null -> PlaceholderBox("Enter a prompt to start")
                    is ImageGenerationResult.Loading -> LoadingBox()
                    is ImageGenerationResult.Success -> ResultImage(targetState.image)
                    is ImageGenerationResult.Error -> ErrorBox(targetState.message)
                }
            }
        }
    }
}
