package com.inoo.chatty.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.inoo.chatty.model.ChatRoomItem

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoom(chatRooms: ChatRoomItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRooms(chatRooms: List<ChatRoomItem>)

    @Query("SELECT * FROM chat_rooms")
    suspend fun getChatRooms(): List<ChatRoomItem>

    @Query("SELECT * FROM chat_rooms WHERE roomId = :roomId")
    suspend fun getChatRoomById(roomId: String): ChatRoomItem?

    @Query("DELETE FROM chat_rooms WHERE roomId = :roomId")
    suspend fun deleteChatRoomById(roomId: String)

    @Query("DELETE FROM chat_rooms")
    suspend fun clearChatRooms()
}