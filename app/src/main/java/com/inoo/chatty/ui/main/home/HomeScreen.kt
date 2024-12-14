package com.inoo.chatty.ui.main.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.inoo.chatty.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {
    val authViewModel = hiltViewModel<AuthViewModel>()
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        TextButton(
            onClick = {
                coroutineScope.launch {
                    authViewModel.logout()
                    onLogout()
                }
            }
        ) {
            Text("Logout")
        }
    }
}