package com.inoo.chatty.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "chat_messages")
data class ChatMessageItem(
    @PrimaryKey
    @field:SerializedName("message_id")
    val messageId: String = "",

    @field:SerializedName("room_id")
    val roomId: String? = null,

    @field:SerializedName("sender_phone_number")
    val senderPhoneNumber: String? = null,

    @field:SerializedName("receiver_phone_number")
    val receiverPhoneNumber: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("deleted_at")
    val deletedAt: String? = null

)