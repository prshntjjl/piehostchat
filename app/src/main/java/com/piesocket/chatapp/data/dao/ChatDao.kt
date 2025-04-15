package com.piesocket.chatapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.piesocket.chatapp.data.entity.Chat

@Dao
interface ChatDao {
    
    @Query("SELECT * FROM chats ORDER BY lastUpdated DESC")
    fun getAllChats(): LiveData<List<Chat>>
    
    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun getChatById(chatId: Long): LiveData<Chat>
    
    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun getChatByIdSync(chatId: Long): Chat?
    
    @Query("SELECT * FROM chats ORDER BY lastUpdated DESC")
    fun getAllChatsSync(): List<Chat>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: Chat): Long
    
    @Update
    suspend fun update(chat: Chat)
    
    @Delete
    suspend fun delete(chat: Chat)
    
    @Query("UPDATE chats SET lastMessage = :lastMessage, lastUpdated = :timestamp WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: Long, lastMessage: String, timestamp: Long)
    
    @Query("UPDATE chats SET unreadCount = unreadCount + 1 WHERE id = :chatId")
    suspend fun incrementUnreadCount(chatId: Long)
    
    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :chatId")
    suspend fun resetUnreadCount(chatId: Long)
    
    @Query("UPDATE chats SET lastMessage = :lastMessage, lastUpdated = :timestamp, unreadCount = unreadCount + 1 WHERE id = :chatId")
    suspend fun updateLastMessageAndIncrementUnread(chatId: Long, lastMessage: String, timestamp: Long)
} 