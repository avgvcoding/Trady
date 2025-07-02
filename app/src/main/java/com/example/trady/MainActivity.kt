package com.example.trady

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize        // <— import for fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.trady.ui.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import com.example.trady.ui.theme.TradyTheme
import com.example.trady.ui.screens.ExploreScreen
import com.example.trady.ui.screens.WatchlistScreen
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.trady.ui.screens.ProductScreen
import com.example.trady.ui.screens.ViewAllScreen
import androidx.navigation.NavHostController


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TradyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()   // now this will resolve
                }
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val items = listOf(Screen.Explore, Screen.Watchlist)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
    ) { innerPadding ->
        NavHost(
            navController  = navController,
            startDestination = Screen.Explore.route,
            modifier       = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Explore.route) { backStack ->
                ExploreScreen(
                    navController = navController
                )
            }
            // New Product destination:
            composable(
                route = Screen.Product.route,
                arguments = listOf(navArgument("symbol") { type = NavType.StringType })
            ) { backStackEntry ->
                val symbol = backStackEntry.arguments?.getString("symbol")!!
                ProductScreen(
                    symbol  = symbol,
                    onBack  = { navController.popBackStack() }   // ← NEW
                )
            }
            composable(Screen.ViewAllGainers.route) {
                ViewAllScreen(
                    isGainers    = true,
                    navController = navController
                )
            }
            composable(Screen.ViewAllLosers.route) {
                ViewAllScreen(
                    isGainers    = false,
                    navController = navController
                )
            }
            composable(Screen.Watchlist.route) {
                WatchlistScreen(navController = navController)
            }

        }
    }
}