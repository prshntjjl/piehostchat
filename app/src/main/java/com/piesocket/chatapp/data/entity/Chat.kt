package com.piesocket.chatapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val lastMessage: String,
    val lastUpdated: Long,
    val unreadCount: Int = 0
) 