package com.saionji.mysensor.shared.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.saionji.mysensor.shared.ui.viewmodel.Screen
import org.jetbrains.compose.resources.painterResource
import com.saionji.mysensor.shared.generated.resources.Res
import com.saionji.mysensor.shared.generated.resources.lists_24px
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon

/**
 * Главный экран приложения с Bottom Navigation
 *
 * Slot API для кроссплатформенного использования.
 *
 * @param currentScreen Текущий экран (Dashboard/Map)
 * @param topBar Слот для TopAppBar
 * @param homeScreen Слот для HomeScreen
 * @param mapScreen Слот для MapScreen
 * @param onDashboardClick Callback при клике на Dashboard
 * @param onMapClick Callback при клике на Map
 */
@Composable
fun MainScreenContent(
    currentScreen: Screen,
    topBar: @Composable () -> Unit,
    homeScreen: @Composable () -> Unit,
    mapScreen: @Composable () -> Unit,
    onDashboardClick: () -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen is Screen.Dashboard,
                    onClick = onDashboardClick,
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.lists_24px),
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Map,
                    onClick = onMapClick,
                    icon = { Icon(Icons.Filled.Map, contentDescription = "Map") },
                    label = { Text("Map") }
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                Screen.Dashboard -> homeScreen()
                Screen.Map -> mapScreen()
            }
        }
    }
}