package com.inoo.chatty.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.inoo.chatty.data.local.dao.ChatDao
import com.inoo.chatty.model.ChatRoomItem

class ChatRepository(
    private val chatDao: ChatDao
) {
    fun getChatRooms(): LiveData<Result<List<ChatRoomItem>>> = liveData {
        emit(Result.Loading)
        try {
            val chatRooms = chatDao.getChatRooms()
            if (chatRooms.isNotEmpty()) {
                emit(Result.Success(chatRooms))
            } else {
                emit(Result.Error("Chat rooms not found"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "An error occurred"))
        }
    }

    fun insertChatRoom(chatRoomItem: ChatRoomItem): LiveData<Result<Unit>> = liveData {
        emit(Result.Loading)
        try {
            chatDao.insertChatRoom(chatRoomItem)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "An error occurred"))
        }
    }

    fun insertChatRooms(chatRooms: List<ChatRoomItem>): LiveData<Result<Unit>> = liveData {
        emit(Result.Loading)
        try {
            chatDao.insertChatRooms(chatRooms)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "An error occurred"))
        }
    }
}