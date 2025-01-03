package com.inoo.chatty.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.inoo.chatty.ui.component.CustomBottomNavigation
import com.inoo.chatty.ui.main.chat.main.ChatScreen
import com.inoo.chatty.ui.main.chat.receiverlist.ReceiverListScreen
import com.inoo.chatty.ui.main.home.HomeScreen
import com.inoo.chatty.ui.main.profile.main.ProfileScreen
import com.inoo.chatty.ui.navigation.Routes

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = when (currentRoute) {
        Routes.HOME, Routes.CHAT, Routes.PROFILE -> true
        else -> false
    }

    var selectedItem by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentRoute) {
        selectedItem = when (currentRoute) {
            Routes.HOME -> 0
            Routes.CHAT -> 1
            Routes.PROFILE -> 2
            else -> selectedItem
        }
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                CustomBottomNavigation(
                    selectedItem = selectedItem,
                    onItemSelected = { index ->
                        selectedItem = index
                        when (index) {
                            0 -> navController.navigate(Routes.HOME) { launchSingleTop = true }
                            1 -> navController.navigate(Routes.CHAT) { launchSingleTop = true }
                            2 -> navController.navigate(Routes.PROFILE) { launchSingleTop = true }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME
            ) {
                composable(Routes.HOME) {
                    HomeScreen()
                }
                composable(Routes.CHAT) {
                    ChatScreen(
                        onNewChatClick = {
                            navController.navigate(Routes.RECEIVER_LIST)
                        },
                        onProfileClick = {},
                        onChatRoomClick = {}
                    )
                }
                composable(Routes.RECEIVER_LIST) {
                    ReceiverListScreen(
                        onContactSelected = {}
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}