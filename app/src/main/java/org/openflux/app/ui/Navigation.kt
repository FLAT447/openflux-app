package org.openflux.app.ui

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.openflux.app.R
import org.openflux.app.data.Profile
import org.openflux.app.data.ProfileDeepLink
import org.openflux.app.ui.home.HomeScreen
import org.openflux.app.ui.profiles.ProfileEditScreen
import org.openflux.app.ui.profiles.ProfileListScreen
import org.openflux.app.ui.settings.SettingsScreen

private sealed class Destination(val route: String, val labelRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Home : Destination("home", R.string.nav_home, Icons.Filled.Home)
    data object Profiles : Destination("profiles", R.string.nav_profiles, Icons.Filled.List)
    data object Settings : Destination("settings", R.string.nav_settings, Icons.Filled.Settings)
}

private const val PROFILE_EDIT_ROUTE = "profile_edit"
private const val PROFILE_ID_ARG = "profileId"

@Composable
fun OpenFluxNavHost(
    onConnectRequested: (String) -> Unit,
    onDisconnectRequested: () -> Unit,
    deepLinkUri: Uri? = null,
    onDeepLinkHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    val tabs = listOf(Destination.Home, Destination.Profiles, Destination.Settings)

    // Set right before navigating to "profile_edit/new" for an imported
    // link, and cleared as soon as that screen is done with it (saved or
    // cancelled) - see the profile_edit composable below.
    var importedProfile by remember { mutableStateOf<Profile?>(null) }

    LaunchedEffect(deepLinkUri) {
        val uri = deepLinkUri ?: return@LaunchedEffect
        ProfileDeepLink.parse(uri)?.let { imported ->
            importedProfile = imported
            navController.navigate("$PROFILE_EDIT_ROUTE/new") { launchSingleTop = true }
        }
        onDeepLinkHandled()
    }

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                // Deliberately no saveState/restoreState: this
                                // is a simple 3-tab app with no deep per-tab
                                // history worth preserving, and restoring a
                                // saved back stack was exactly what caused a
                                // stale "new profile" draft (or the wrong
                                // screen) to reappear after leaving it
                                // mid-edit and coming back. Tapping a tab
                                // always fully resets to that tab's root.
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResourceCompat(tab.labelRes)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = androidx.compose.ui.Modifier.padding(padding),
        ) {
            composable(Destination.Home.route) {
                HomeScreen(
                    onConnectRequested = onConnectRequested,
                    onDisconnectRequested = onDisconnectRequested,
                    onManageProfiles = { navController.navigate(Destination.Profiles.route) },
                    onEditProfile = { id -> navController.navigate("$PROFILE_EDIT_ROUTE/$id") },
                )
            }
            composable(Destination.Profiles.route) {
                ProfileListScreen(
                    onAddProfile = { navController.navigate("$PROFILE_EDIT_ROUTE/new") },
                    onEditProfile = { id -> navController.navigate("$PROFILE_EDIT_ROUTE/$id") },
                )
            }
            composable(
                route = "$PROFILE_EDIT_ROUTE/{$PROFILE_ID_ARG}",
            ) { backStackEntry ->
                val rawId = backStackEntry.arguments?.getString(PROFILE_ID_ARG)
                val isNew = rawId == null || rawId == "new"

                // Clears importedProfile no matter how this screen is left -
                // the system back gesture/button pops it without going
                // through onDone, and leaving it set would silently prefill
                // the *next* "add profile" with this stale imported data.
                androidx.compose.runtime.DisposableEffect(Unit) {
                    onDispose { importedProfile = null }
                }

                ProfileEditScreen(
                    profileId = rawId?.takeIf { !isNew },
                    importedProfile = if (isNew) importedProfile else null,
                    onDone = { navController.popBackStack() },
                )
            }
            composable(Destination.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

@Composable
private fun stringResourceCompat(resId: Int): String = androidx.compose.ui.res.stringResource(id = resId)
