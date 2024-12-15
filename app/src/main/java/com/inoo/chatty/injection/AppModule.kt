package com.inoo.chatty.injection

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.inoo.chatty.data.local.dao.ChatDao
import com.inoo.chatty.data.local.dao.UserDao
import com.inoo.chatty.data.local.database.ChattyDatabase
import com.inoo.chatty.data.preference.ChattyAppPreferences
import com.inoo.chatty.repository.ChatRepository
import com.inoo.chatty.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Singleton
    @Provides
    fun provideChattyAppPreferences(context: Context): ChattyAppPreferences {
        return ChattyAppPreferences(context)
    }

    @Singleton
    @Provides
    fun provideChattyDatabase(context: Context): ChattyDatabase {
        return Room.databaseBuilder(
            context,
            ChattyDatabase::class.java,
            "chatty_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideUserDao(chattyDatabase: ChattyDatabase): UserDao {
        return chattyDatabase.userDao()
    }

    @Singleton
    @Provides
    fun provideChatDao(chattyDatabase: ChattyDatabase): ChatDao {
        return chattyDatabase.chatDao()
    }

    @Singleton
    @Provides
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepository(userDao)
    }

    @Singleton
    @Provides
    fun provideChatRepository(chatDao: ChatDao): ChatRepository {
        return ChatRepository(chatDao)
    }
}