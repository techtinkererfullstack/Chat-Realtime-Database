package com.example.chatapp

data class Message(
    val senderId: String? = null,
    val message: String? = null,
    val timestamp: Long? = null
)