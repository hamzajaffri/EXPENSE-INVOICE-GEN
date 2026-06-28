package com.example.data

import android.content.Context

interface AppContainer {
    val repository: BusinessRepository
    val preferenceStore: PreferenceStore
}

class AppContainerImpl(private val context: Context) : AppContainer {
    override val repository: BusinessRepository by lazy {
        BusinessRepository(AppDatabase.getDatabase(context))
    }

    override val preferenceStore: PreferenceStore by lazy {
        PreferenceStore(context)
    }
}
