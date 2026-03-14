package com.inspectavision.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ModelSelector : Screen("model_selector")
    object InspectionCamera : Screen("inspection_camera")
    object Analysis : Screen("analysis/{imageUri}") {
        fun createRoute(imageUri: String) = "analysis/$imageUri"
    }
    object FindingsList : Screen("findings")
    object Settings : Screen("settings")
}
