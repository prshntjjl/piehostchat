package com.piesocket.chatapp

import android.app.Application
import com.piesocket.chatapp.data.AppDatabase
import com.piesocket.chatapp.network.WebSocketManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class ChatApplication : Application()