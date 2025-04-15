package com.piesocket.chatapp.network

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.piesocket.chatapp.util.NetworkUtils
import com.piesocket.chatapp.util.SingleLiveEvent
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketManager(private val context: Context) {
    
    private val TAG = "WebSocketManager"
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Track recent sent messages to avoid duplicate handling due to notify_self
    private val recentSentMessages = mutableSetOf<String>()

    private val _connectionStatus = MutableLiveData<ConnectionStatus>()
    val connectionStatus: LiveData<ConnectionStatus> = _connectionStatus
    
    // Use SingleLiveEvent for message events to ensure they're only processed once
    private val _messageReceived = SingleLiveEvent<WebSocketMessage>()
    val messageReceived: LiveData<WebSocketMessage> = _messageReceived
    
    fun connect() {
        if (webSocket != null) {
            return
        }
        
        val request = Request.Builder()
            .url("wss://demo.piesocket.com/v3/channel_123?api_key=II0uuOMujklGbT5wfPpykzoKxRem036K1T80IcfC&notify_self=1")
            .build()
        
        _connectionStatus.postValue(ConnectionStatus.CONNECTING)
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connection opened")
                _connectionStatus.postValue(ConnectionStatus.CONNECTED)
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
                try {
                    
                    val jsonObject = JSONObject(text)
                    if(jsonObject.has("error")) {
                        return
                    }
                    val senderId = jsonObject.optString("senderId", "")
                    val message = jsonObject.optString("message", "")
                    val timestamp = jsonObject.optLong("timestamp", System.currentTimeMillis())
                    val chatId = jsonObject.optLong("chatId", 0)
                    
                    // Create a unique key for this message
                    val messageKey = "$senderId:$message:$timestamp:$chatId"
                    
                    // Skip if this is a message we just sent (due to notify_self=1)
                    if (senderId == "user" && recentSentMessages.contains(messageKey)) {
                        Log.d(TAG, "Ignoring self-notification message: $messageKey")
                        return
                    }

                    val webSocketMessage = WebSocketMessage(
                        chatId = chatId,
                        senderId = senderId,
                        message = message,
                        timestamp = timestamp
                    )
                    
                    // With SingleLiveEvent, we don't need to track the last message
                    // Post the value on the main thread to ensure observers are notified immediately
                    Log.d(TAG, "Posting new websocket message: $webSocketMessage")
                    _messageReceived.postValue(webSocketMessage)
                    
                    // Also post a connection status update to trigger UI refresh
                    if (_connectionStatus.value == ConnectionStatus.CONNECTED) {
                        _connectionStatus.postValue(ConnectionStatus.CONNECTED)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message", e)
                }
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket connection closed: $reason")
                this@WebSocketManager.webSocket = null
                _connectionStatus.postValue(ConnectionStatus.DISCONNECTED)
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure", t)
                this@WebSocketManager.webSocket = null
                _connectionStatus.postValue(ConnectionStatus.ERROR)
            }
        })
    }
    
    fun disconnect() {
        webSocket?.close(1000, "User closed connection")
        webSocket = null
        _connectionStatus.postValue(ConnectionStatus.DISCONNECTED)
    }
    
    fun sendMessage(chatId: Long, message: String): Boolean {
        if (webSocket == null || !NetworkUtils.isNetworkConnected(context)) {
            Log.d(TAG, "Network not available or WebSocket not connected, message will be queued")
            return false
        }
        
        try {
            val timestamp = System.currentTimeMillis()
            val jsonObject = JSONObject().apply {
                put("chatId", chatId)
                put("senderId", "user")
                put("message", message)
                put("timestamp", timestamp)
            }
            
            // Record this message as sent to avoid handling it again when it comes back
            val messageKey = "user:$message:$timestamp:$chatId"
            recentSentMessages.add(messageKey)

            return webSocket?.send(jsonObject.toString()) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            return false
        }
    }
    
    enum class ConnectionStatus {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        ERROR
    }
    
    data class WebSocketMessage(
        val chatId: Long,
        val senderId: String,
        val message: String,
        val timestamp: Long
    )
}