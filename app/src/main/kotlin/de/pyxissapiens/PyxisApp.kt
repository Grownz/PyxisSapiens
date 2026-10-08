package de.pyxissapiens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.pyxissapiens.core.ui.gallery.DesignGallery
import de.pyxissapiens.feature.compass.CompassRoute
import de.pyxissapiens.feature.map.MapRoute
import de.pyxissapiens.feature.projects.ProjectsRoute
import de.pyxissapiens.feature.settings.SettingsRoute
import de.pyxissapiens.feature.stereonet.StereonetRoute

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination("compass", "Messen", Icons.Filled.Explore),
    BottomDestination("data", "Daten", Icons.AutoMirrored.Filled.List),
    BottomDestination("map", "Karte", Icons.Filled.Map),
    BottomDestination("analysis", "Auswertung", Icons.Filled.BarChart),
    BottomDestination("more", "Mehr", Icons.Filled.MoreHoriz),
)

@Composable
fun PyxisApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                bottomDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo("compass") { saveState = true }
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "compass",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("compass") { CompassRoute() }
            composable("data") { ProjectsRoute() }
            composable("map") { MapRoute() }
            composable("analysis") { StereonetRoute() }
            composable("more") {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    SettingsRoute(Modifier.weight(1f))
                    Button(
                        onClick = { navController.navigate("gallery") },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Design-Galerie öffnen")
                    }
                }
            }
            composable("gallery") { DesignGallery() }
        }
    }
}
