package com.inoo.chatty.injection

import android.app.Application
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.inoo.chatty.data.preference.ChattyAppPreferences
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
    fun provideChattyAppPreferences(context: Context): ChattyAppPreferences {
        return ChattyAppPreferences(context)
    }
}