package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NyintCyan
import com.example.ui.viewmodel.MainViewModel

data class NavTabItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isTabletOrLandscape = configuration.screenWidthDp >= 600

    val navItems = listOf(
        NavTabItem("home", "Home", Icons.Default.Home, "nav_home"),
        NavTabItem("chat", "Chatbox", Icons.Default.ChatBubble, "nav_chat"),
        NavTabItem("create", "Create", Icons.Default.AutoAwesome, "nav_create"),
        NavTabItem("agents", "Agents", Icons.Default.SmartToy, "nav_agents"),
        NavTabItem("history", "History", Icons.Default.History, "nav_history"),
        NavTabItem("settings", "Settings", Icons.Default.Settings, "nav_settings")
    )

    // Back handling
    BackHandler(enabled = uiState.currentTab != "chat") {
        viewModel.setNavigationTab("chat")
    }

    if (isTabletOrLandscape) {
        // Tablet / Large Screen Layout with NavigationRail
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = NyintCyan,
                modifier = Modifier.testTag("tablet_nav_rail")
            ) {
                navItems.forEach { item ->
                    val isSelected = uiState.currentTab == item.id
                    NavigationRailItem(
                        selected = isSelected,
                        onClick = { viewModel.setNavigationTab(item.id) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(item.title, fontSize = 11.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NyintCyan,
                            indicatorColor = NyintCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                ScreenContent(
                    tab = uiState.currentTab,
                    viewModel = viewModel
                )
            }
        }
    } else {
        // Mobile Layout with Bottom NavigationBar
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    navItems.forEach { item ->
                        val isSelected = uiState.currentTab == item.id
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setNavigationTab(item.id) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = { Text(item.title, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = NyintCyan,
                                indicatorColor = NyintCyan,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ScreenContent(
                    tab = uiState.currentTab,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun ScreenContent(
    tab: String,
    viewModel: MainViewModel
) {
    when (tab) {
        "home" -> HomeScreen(
            viewModel = viewModel,
            onNavigateToChat = { viewModel.setNavigationTab("chat") },
            onOpenCreateStudio = { viewModel.setNavigationTab("create") },
            onOpenAgents = { viewModel.setNavigationTab("agents") },
            onOpenSettings = { viewModel.setNavigationTab("settings") }
        )
        "chat" -> ChatboxScreen(
            viewModel = viewModel,
            onOpenSettings = { viewModel.setNavigationTab("settings") },
            onOpenCreateStudio = { viewModel.setNavigationTab("create") }
        )
        "create" -> CreateStudioScreen(
            viewModel = viewModel,
            onNavigateToChat = { viewModel.setNavigationTab("chat") }
        )
        "agents" -> AgentsWorkspaceScreen(
            viewModel = viewModel,
            onNavigateToChat = { viewModel.setNavigationTab("chat") }
        )
        "history" -> HistoryScreen(
            viewModel = viewModel,
            onNavigateToChat = { viewModel.setNavigationTab("chat") }
        )
        "settings" -> SettingsScreen(
            viewModel = viewModel
        )
        else -> ChatboxScreen(
            viewModel = viewModel,
            onOpenSettings = { viewModel.setNavigationTab("settings") },
            onOpenCreateStudio = { viewModel.setNavigationTab("create") }
        )
    }
}
