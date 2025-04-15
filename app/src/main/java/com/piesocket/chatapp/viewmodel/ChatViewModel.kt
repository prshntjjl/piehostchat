package com.piesocket.chatapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.piesocket.chatapp.ChatApplication
import com.piesocket.chatapp.data.AppDatabase
import com.piesocket.chatapp.data.entity.Chat
import com.piesocket.chatapp.data.entity.Message
import com.piesocket.chatapp.network.WebSocketManager
import com.piesocket.chatapp.repository.ChatRepository
import com.piesocket.chatapp.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    application: Application,
    private val repository: ChatRepository,
    val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {
    
    val allChats: LiveData<List<Chat>> = repository.allChats
    val isNetworkAvailable: LiveData<Boolean> = NetworkUtils.isNetworkAvailable
    val webSocketStatus: LiveData<WebSocketManager.ConnectionStatus> = webSocketManager.connectionStatus
    
    // Store observers so we can remove them later
    private val messageObserver = Observer<WebSocketManager.WebSocketMessage> { webSocketMessage ->
        viewModelScope.launch {
            // Save received message to database
            val message = Message(
                id = 0,
                chatId = webSocketMessage.chatId,
                content = webSocketMessage.message,
                timestamp = webSocketMessage.timestamp,
                isOutgoing = false,  // Always false for received messages
                status = Message.STATUS_DELIVERED
            )
            
            // Process the received message and force UI refresh
            repository.saveReceivedMessage(message)
            
            // Force refresh of chats, if needed
            if (allChats.hasActiveObservers()) {
                val app = getApplication<Application>()
                viewModelScope.launch(Dispatchers.Main) {
                    // Refresh happens automatically through LiveData, but we can trigger notifications
                    // by logging to help debug
                    android.util.Log.d("ChatViewModel", "Received message in chat list: ${webSocketMessage.message}")
                }
            }
        }
    }
    
    private val networkObserver = Observer<Boolean> { isAvailable ->
        if (isAvailable) {
            webSocketManager.connect()
            repository.retryQueuedMessages()
        } else {
            webSocketManager.disconnect()
        }
    }
    
    init {
        // Initialize network utilities
        NetworkUtils.init(application)
        
        // Register observers
        isNetworkAvailable.observeForever(networkObserver)
        webSocketManager.messageReceived.observeForever(messageObserver)
        
        // Connect to WebSocket
        webSocketManager.connect()
    }
    
    fun getChatMessages(chatId: Long): LiveData<List<Message>> {
        return repository.getMessagesForChat(chatId)
    }
    
    fun getChat(chatId: Long): LiveData<Chat> {
        return repository.getChat(chatId)
    }
    
    fun sendMessage(chatId: Long, content: String) {
        viewModelScope.launch {
            repository.sendMessage(chatId, content)
        }
    }
    
    fun retryQueuedMessages() {
        viewModelScope.launch {
            repository.retryQueuedMessages()
        }
    }
    
    fun markChatAsRead(chatId: Long) {
        viewModelScope.launch {
            repository.markChatAsRead(chatId)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // Remove observers to prevent leaks
        webSocketManager.messageReceived.removeObserver(messageObserver)
        isNetworkAvailable.removeObserver(networkObserver)
        
        webSocketManager.disconnect()
    }
} 