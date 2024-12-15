package com.inoo.chatty.model

data class Contact(
    val name: String,
    val phoneNumber: String,
    val profilePicture: String? = null
)