package com.inoo.chatty.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.PropertyName

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    @PropertyName("uid")
    val uid: String = "",

    @PropertyName("name")
    val name: String? = null,

    @PropertyName("phone_number")
    val phoneNumber: String = "",

    @PropertyName("birth_date")
    val birthDate: String? = null,

    @PropertyName("email")
    val email: String? = null,

    @PropertyName("profile_photo_url")
    val profilePicture: String? = null,

    @PropertyName("created_at")
    val createdAt: String? = null
)