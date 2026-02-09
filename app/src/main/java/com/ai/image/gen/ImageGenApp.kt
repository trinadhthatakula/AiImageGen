package com.ai.image.gen

import android.app.Application
import com.ai.image.gen.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ImageGenApp  : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Log Koin errors/info (Helpful for debugging)
            androidLogger(Level.ERROR)
            // Reference Android context
            androidContext(this@ImageGenApp)
            // Load modules
            modules(appModule)
        }
    }
}