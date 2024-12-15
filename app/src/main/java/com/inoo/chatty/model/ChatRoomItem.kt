package com.inoo.chatty.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "chat_rooms")
data class ChatRoomItem(
    @PrimaryKey
    @field:SerializedName("room_id")
    val roomId: String = "",

    @field:SerializedName("sender_name")
    val senderName: String? = null,

    @field:SerializedName("receiver_name")
    val receiverName: String? = null,

    @field:SerializedName("sender_phone_number")
    val senderPhoneNumber: String? = null,

    @field:SerializedName("receiver_phone_number")
    val receiverPhoneNumber: String? = null,

    @field:SerializedName("sender_profile_photo_url")
    val senderProfilePicture: String? = null,

    @field:SerializedName("receiver_profile_photo_url")
    val receiverProfilePicture: String? = null,

    @field:SerializedName("last_message")
    val lastMessage: String? = null,

    @field:SerializedName("last_message_time")
    val lastMessageTime: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("deleted_at")
    val deletedAt: String? = null
)