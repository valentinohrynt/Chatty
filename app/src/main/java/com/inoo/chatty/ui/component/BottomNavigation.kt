package com.inoo.chatty.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inoo.chatty.ui.navigation.Routes

@Composable
fun CustomBottomNavigation(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            icon = Icons.Filled.Home,
            label = "Home",
            route = Routes.HOME
        ),
        BottomNavItem(
            icon = Icons.Filled.ChatBubbleOutline,
            label = "Chat",
            route = Routes.CHAT
        ),
        BottomNavItem(
            icon = Icons.Filled.Person,
            label = "Profile",
            route = Routes.PROFILE
        )
    )

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = TextStyle(
                            color = if (selectedItem == index)
                                Color.White
                            else Color.White.copy(alpha = 0.6f),
                            fontWeight = if (selectedItem == index)
                                FontWeight.Bold
                            else FontWeight.Normal
                        )
                    )
                },
                selected = selectedItem == index,
                onClick = { onItemSelected(index) }
            )
        }
    }
}

data class BottomNavItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)
