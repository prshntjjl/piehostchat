package com.piesocket.chatapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.piesocket.chatapp.data.entity.Message

@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: Long): LiveData<List<Message>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message): Long
    
    @Update
    suspend fun update(message: Message)
    
    @Delete
    suspend fun delete(message: Message)
    
    @Query("SELECT * FROM messages WHERE status = :status")
    suspend fun getMessagesByStatus(status: Int): List<Message>
    
    @Query("UPDATE messages SET status = :newStatus WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: Long, newStatus: Int)

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND content = :content AND timestamp BETWEEN :timestampMin AND :timestampMax")
    suspend fun getMessagesByContentAndTimeRange(chatId: Long, content: String, timestampMin: Long, timestampMax: Long): List<Message>
} 