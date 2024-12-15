package com.inoo.chatty.ui.main.chat.main

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.inoo.chatty.model.ChatRoomItem
import com.inoo.chatty.model.User
import com.inoo.chatty.repository.ChatRepository
import com.inoo.chatty.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.inoo.chatty.repository.Result

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val firebaseDatabase: FirebaseDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val _chatRooms = MutableStateFlow<List<ChatRoomItem>>(emptyList())
    val chatRooms = _chatRooms.asStateFlow()

    private val _userData = MutableStateFlow<User?>(null)
    val userData = _userData.asStateFlow()

    init {
        loadChatRooms()
    }

    fun loadChatRooms() {
        _uiState.value = uiState.value.copy(isLoading = true)
        userRepository.getUserData().observeForever { result ->
            when (result) {
                is Result.Success -> {
                    _uiState.value =
                        uiState.value.copy(success = "User data loaded", isLoading = false)
                    _userData.value = result.data

                    try {
                        val userChatsRef = firebaseDatabase.getReference("userChats")
                            .child(userData.value?.phoneNumber.toString())

                        userChatsRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val chatRooms = mutableListOf<ChatRoomItem>()
                                for (child in snapshot.children) {
                                    val chatRoom = child.getValue(ChatRoomItem::class.java)
                                    chatRooms.add(chatRoom!!)
                                }
                                chatRepository.insertChatRooms(chatRooms).observeForever { result ->
                                    when (result) {
                                        is Result.Success -> {
                                            _uiState.value = uiState.value.copy(
                                                success = "Chat rooms loaded",
                                                isLoading = false
                                            )
                                            _chatRooms.value =
                                                chatRooms.sortedByDescending { it.updatedAt }
                                        }

                                        is Result.Error -> {
                                            _uiState.value = uiState.value.copy(
                                                error = result.error,
                                                isLoading = false
                                            )
                                        }

                                        is Result.Loading -> {
                                            _uiState.value = uiState.value.copy(isLoading = true)
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                _uiState.value =
                                    uiState.value.copy(error = error.message, isLoading = false)
                            }
                        })
                    } catch (e: Exception) {
                        _uiState.value = uiState.value.copy(error = e.message, isLoading = false)
                    }
                }

                is Result.Error -> {
                    _uiState.value = uiState.value.copy(error = result.error, isLoading = false)
                }

                is Result.Loading -> {
                    _uiState.value = uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun createNewChatRoom(
        receiverPhoneNumber: String,
        onSuccess: (chatRoomId: String) -> Unit
    ) {
        val chatRef = firebaseDatabase.reference.child("chatRooms").push()
        val chatRoomId = chatRef.key ?: ""

        val member = mapOf(
            "member1" to mapOf(
                "phoneNumber" to userData.value?.phoneNumber,
                "name" to userData.value?.name,
                "profilePicture" to userData.value?.profilePicture
            ),
            "member2" to mapOf(
                "phoneNumber" to "081246697653",
                "name" to "Valentino",
                "profilePicture" to "https://www.google.com"
            )
        )

        val initial = hashMapOf(
            "last_message" to null,
            "last_message_time" to null,
            "members" to member,
            "created_at" to System.currentTimeMillis().toString(),
            "updated_at" to System.currentTimeMillis().toString(),
            "deleted_at" to null
        )

        chatRef.setValue(initial).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userChatsRef = firebaseDatabase.reference.child("userChats")
                    .child(userData.value?.phoneNumber.toString())
                userChatsRef.push().setValue(chatRoomId).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _uiState.value = uiState.value.copy(success = "Chat room created")
                        onSuccess(chatRoomId)
                    } else {
                        _uiState.value = uiState.value.copy(error = "Failed to create chat room")
                    }
                }
            } else {
                _uiState.value = uiState.value.copy(error = "Failed to create chat room")
            }
        }
    }
}

data class ChatScreenUiState(
    val isLoading: Boolean = false,
    val success: String? = null,
    val error: String? = null
)