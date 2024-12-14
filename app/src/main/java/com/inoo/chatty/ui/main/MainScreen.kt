package com.inoo.chatty.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.inoo.chatty.ui.main.home.HomeScreen
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onLogout = onLogout
                )
            }
            composable(Routes.CHAT) {

            }
            composable(Routes.PROFILE) {

            }
        }
        if (shouldShowBottomBar) {
            CustomBottomNavigation(
                modifier = Modifier.align(Alignment.BottomCenter),
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it }
            )
        }
    }
}