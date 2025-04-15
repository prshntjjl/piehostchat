package com.piesocket.chatapp.di

import android.app.Application
import com.piesocket.chatapp.data.AppDatabase
import com.piesocket.chatapp.network.WebSocketManager
import com.piesocket.chatapp.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application, scope: CoroutineScope): AppDatabase {
        return AppDatabase.getDatabase(app, scope)
    }

    @Provides
    @Singleton
    fun provideWebSocketManager(app: Application): WebSocketManager {
        return WebSocketManager(app)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        database: AppDatabase,
        webSocketManager: WebSocketManager,
        scope: CoroutineScope
    ): ChatRepository {
        return ChatRepository(
            database.chatDao(),
            database.messageDao(),
            webSocketManager,
            scope
        )
    }
} 