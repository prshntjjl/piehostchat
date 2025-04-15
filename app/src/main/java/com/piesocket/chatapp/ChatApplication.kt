package com.piesocket.chatapp

import android.app.Application
import com.piesocket.chatapp.data.AppDatabase
import com.piesocket.chatapp.network.WebSocketManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class ChatApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob())
    
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val webSocketManager by lazy { WebSocketManager(this) }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: ChatApplication
            private set
    }
} 