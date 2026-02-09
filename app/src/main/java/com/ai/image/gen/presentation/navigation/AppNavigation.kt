package com.ai.image.gen.presentation.navigation

import android.app.Activity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ai.image.gen.data.worker.ImageEditWorker
import com.ai.image.gen.presentation.home.HomeScreen
import com.ai.image.gen.presentation.i2i.EditResultScreen
import com.ai.image.gen.presentation.i2i.ImageEditScreen
import com.ai.image.gen.presentation.saved.SavedImagesScreen
import com.ai.image.gen.presentation.t2i.TextToImageScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as? Activity

    // Determine if we should show the bottom bar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show bottom bar on top-level routes
    val topLevelRoutes = listOf(Screen.Home.route, Screen.Saved.route)
    val showBottomBar = topLevelRoutes.any { route ->
        currentDestination?.hierarchy?.any { it.route == route } == true
    }

    LaunchedEffect(Unit) {
        activity?.intent?.let { intent ->
            val path = intent.getStringExtra(ImageEditWorker.KEY_RESULT_PATH)
            if (path != null) {
                // Remove the extra so we don't reopen it on rotation
                intent.removeExtra(ImageEditWorker.KEY_RESULT_PATH)
                navController.navigate(Screen.EditResult.createRoute(path))
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = listOf(Screen.Home, Screen.Saved)
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = null) },
                            label = { Text(screen.title!!) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // 1. Home Tab
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToT2I = { navController.navigate(Screen.TextToImage.route) },
                    onNavigateToEdit = { navController.navigate(Screen.ImageEdit.route) }
                )
            }

            // 2. Saved Tab
            composable(Screen.Saved.route) {
                SavedImagesScreen(
                    onImageClick = { filePath ->
                        navController.navigate(Screen.EditResult.createRoute(filePath))
                    }
                )
            }

            // 3. Text to Image (Leaf Screen)
            composable(Screen.TextToImage.route) {
                TextToImageScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 4. Image Edit (Leaf Screen)
            composable(Screen.ImageEdit.route) {
                ImageEditScreen(
                    onNavigateHome = {
                        // Pop back to home after queuing
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.EditResult.route,
                arguments = listOf(navArgument("encodedPath") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedPath = backStackEntry.arguments?.getString("encodedPath") ?: ""
                // Decode the URL-safe string back to a real file path
                val realPath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())

                EditResultScreen(
                    filePath = realPath,
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}