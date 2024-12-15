package com.inoo.chatty.ui.main.chat.main

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.inoo.chatty.R
import com.inoo.chatty.model.ChatRoomItem
import com.inoo.chatty.ui.component.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onChatRoomClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onNewChatClick: () -> Unit
) {
    val viewModel = hiltViewModel<ChatViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val chatRooms by viewModel.chatRooms.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredChatRooms by remember(searchQuery, chatRooms) {
        derivedStateOf {
            chatRooms.filter { chatRoom ->
                chatRoom.receiverName?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.new_chat)
                )
            }
        }
    ) { paddingValues ->
        Log.d("padding", "$paddingValues")
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = stringResource(id = R.string.chat),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    SearchBar(
                        inputField = {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                decorationBox = { innerTextField ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = stringResource(id = R.string.search),
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (searchQuery.isEmpty()) {
                                                Text(
                                                    text = stringResource(id = R.string.search),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                }
                            )
                        },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .height(56.dp)
                    ) {
                        filteredChatRooms.forEach { chatRoom ->
                            ListItem(
                                headlineContent = { Text(chatRoom.receiverName.orEmpty()) },
                                supportingContent = { Text(chatRoom.lastMessage.orEmpty()) },
                                modifier = Modifier.clickable {
                                    onChatRoomClick(chatRoom.roomId)
                                    expanded = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (chatRooms.isEmpty() && !uiState.isLoading) {
                        EmptyState(
                            emptyTitle = stringResource(id = R.string.no_messages),
                            emptyDescription = stringResource(id = R.string.no_messages_desc)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        ) {
                            itemsIndexed(chatRooms) { _, item ->
                                ChatRoomItem(
                                    chatRoom = item,
                                    onClick = { onChatRoomClick(item.roomId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRoomItem(
    chatRoom: ChatRoomItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(chatRoom.receiverProfilePicture)
                .crossfade(true)
                .scale(Scale.FILL)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = chatRoom.receiverName.orEmpty(),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = chatRoom.lastMessage.orEmpty(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}