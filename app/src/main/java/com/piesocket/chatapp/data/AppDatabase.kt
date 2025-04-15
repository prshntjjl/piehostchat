package com.piesocket.chatapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.piesocket.chatapp.data.dao.ChatDao
import com.piesocket.chatapp.data.dao.MessageDao
import com.piesocket.chatapp.data.entity.Chat
import com.piesocket.chatapp.data.entity.Message
import com.piesocket.chatapp.util.DateConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Chat::class, Message::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chat_database"
                )
                .addCallback(ChatDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class ChatDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(database: AppDatabase) {
            // Create initial chats (bots)
            val chatDao = database.chatDao()
            
            chatDao.insert(Chat(0, "SupportBot", "Need help? Ask me anything.", System.currentTimeMillis()))
            chatDao.insert(Chat(0, "SalesBot", "Looking for our products? I can help!", System.currentTimeMillis()))
            chatDao.insert(Chat(0, "FAQBot", "I can answer frequently asked questions.", System.currentTimeMillis()))
        }
    }
} 