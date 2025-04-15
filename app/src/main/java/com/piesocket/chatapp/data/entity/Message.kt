package com.piesocket.chatapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Chat::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chatId")]
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val chatId: Long,
    val content: String,
    val timestamp: Long,
    val isOutgoing: Boolean,
    val status: Int // 0 = SENT, 1 = DELIVERED, 2 = READ, 3 = QUEUED
) {
    companion object {
        const val STATUS_SENT = 0
        const val STATUS_DELIVERED = 1
        const val STATUS_READ = 2
        const val STATUS_QUEUED = 3
    }
} 