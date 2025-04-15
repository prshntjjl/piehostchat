package com.piesocket.chatapp.repository

import androidx.lifecycle.LiveData
import com.piesocket.chatapp.data.dao.ChatDao
import com.piesocket.chatapp.data.dao.MessageDao
import com.piesocket.chatapp.data.entity.Chat
import com.piesocket.chatapp.data.entity.Message
import com.piesocket.chatapp.network.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val webSocketManager: WebSocketManager,
    private val applicationScope: CoroutineScope
) {
    
    val allChats = chatDao.getAllChats()
    
    fun getChat(chatId: Long): LiveData<Chat> {
        return chatDao.getChatById(chatId)
    }
    
    fun getMessagesForChat(chatId: Long): LiveData<List<Message>> {
        return messageDao.getMessagesForChat(chatId)
    }
    
    suspend fun sendMessage(chatId: Long, content: String): Long {
        val timestamp = System.currentTimeMillis()
        
        // Create message entity first
        val message = Message(
            id = 0,
            chatId = chatId,
            content = content,
            timestamp = timestamp,
            isOutgoing = true,
            status = Message.STATUS_QUEUED
        )
        
        // Insert into local database
        val messageId = withContext(Dispatchers.IO) {
            messageDao.insert(message)
        }
        
        // Update chat with last message
        withContext(Dispatchers.IO) {
            chatDao.updateLastMessage(chatId, content, timestamp)
        }
        
        // Try to send via WebSocket after database operations
        val sent = webSocketManager.sendMessage(chatId, content)
        
        // Update status if sent successfully
        if (sent) {
            withContext(Dispatchers.IO) {
                messageDao.updateMessageStatus(messageId, Message.STATUS_SENT)
            }
        } else {
            // If message was queued, we'll retry when connection is available
            monitorQueuedMessages()
        }
        
        return messageId
    }
    
    suspend fun saveReceivedMessage(message: Message) {
        withContext(Dispatchers.IO) {
            // Debug log start
            android.util.Log.d("ChatRepository", "Saving received message: ${message.content} for chat: ${message.chatId}")
            
            // First check if we have this message already
            val timestampMin = message.timestamp - 1000
            val timestampMax = message.timestamp + 1000
            
            val existingMessages = messageDao.getMessagesByContentAndTimeRange(
                message.chatId, 
                message.content, 
                timestampMin, 
                timestampMax
            )
            
            if (existingMessages.isEmpty()) {
                val messageId = messageDao.insert(message)
                
                // Update chat's last message and increment unread count
                val chat = chatDao.getChatByIdSync(message.chatId)
                if (chat != null) {
                    // Only count unread for incoming messages (not outgoing ones)
                    if (!message.isOutgoing) {
                        // Update with the latest timestamp and increment unread count
                        chatDao.updateLastMessageAndIncrementUnread(
                            message.chatId, 
                            message.content, 
                            System.currentTimeMillis()
                        )
                    } else {
                        // Just update the last message without incrementing unread
                        chatDao.updateLastMessage(
                            message.chatId, 
                            message.content, 
                            System.currentTimeMillis()
                        )
                    }
                }
            }
        }
    }
    
    private fun monitorQueuedMessages() {
        applicationScope.launch {
            // Get all queued messages
            val queuedMessages = withContext(Dispatchers.IO) {
                messageDao.getMessagesByStatus(Message.STATUS_QUEUED)
            }
            
            if (queuedMessages.isEmpty()) {
                return@launch
            }
            
            // Try to send all queued messages
            for (message in queuedMessages) {
                val sent = webSocketManager.sendMessage(message.chatId, message.content)
                if (sent) {
                    // Update the message status if successfully sent
                    withContext(Dispatchers.IO) {
                        messageDao.updateMessageStatus(message.id, Message.STATUS_SENT)
                    }
                }
            }
        }
    }
    
    fun retryQueuedMessages() {
        monitorQueuedMessages()
    }
    
    suspend fun markChatAsRead(chatId: Long) {
        withContext(Dispatchers.IO) {
            // Reset unread count for this chat
            chatDao.resetUnreadCount(chatId)
        }
    }
} 