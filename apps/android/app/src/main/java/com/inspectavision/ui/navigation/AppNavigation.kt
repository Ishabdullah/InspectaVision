package com.inspectavision.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.inspectavision.llm.LlamaCppBridge
import com.inspectavision.ui.screens.HomeScreen
import com.inspectavision.ui.screens.ModelSelectorScreen
import com.inspectavision.ui.screens.InspectionCameraScreen
import com.inspectavision.ui.screens.AnalysisScreen
import com.inspectavision.ui.screens.FindingsListScreen
import com.inspectavision.ui.screens.SettingsScreen

@Composable
fun AppNavigation(
    isModelLoaded: Boolean,
    llamaBridge: LlamaCppBridge,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = if (isModelLoaded) Screen.Home.route else Screen.ModelSelector.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCamera = { navController.navigate(Screen.InspectionCamera.route) },
                onNavigateToFindings = { navController.navigate(Screen.FindingsList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToModelSelector = { navController.navigate(Screen.ModelSelector.route) }
            )
        }
        
        composable(Screen.ModelSelector.route) {
            ModelSelectorScreen(
                llamaBridge = llamaBridge,
                onModelLoaded = { navController.navigate(Screen.Home.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.InspectionCamera.route) {
            InspectionCameraScreen(
                onImageCaptured = { uri ->
                    navController.navigate(Screen.Analysis.createRoute(uri))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Analysis.route,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            AnalysisScreen(
                imageUri = imageUri,
                llamaBridge = llamaBridge,
                onNavigateBack = { navController.popBackStack() },
                onSaveFinding = { navController.popBackStack() }
            )
        }
        
        composable(Screen.FindingsList.route) {
            FindingsListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                llamaBridge = llamaBridge,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
