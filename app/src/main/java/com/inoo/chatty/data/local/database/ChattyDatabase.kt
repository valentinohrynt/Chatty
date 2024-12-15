package com.inoo.chatty.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inoo.chatty.data.local.dao.ChatDao
import com.inoo.chatty.data.local.dao.UserDao
import com.inoo.chatty.model.ChatMessageItem
import com.inoo.chatty.model.ChatRoomItem
import com.inoo.chatty.model.User

@Database(
    entities = [User::class, ChatRoomItem::class, ChatMessageItem::class],
    version = 1,
    exportSchema = false
)
abstract class ChattyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao
}