package com.ai.image.gen.di

import androidx.room.Room
import androidx.work.WorkManager
import com.ai.image.gen.BuildConfig
import com.ai.image.gen.data.HuggingFaceApi
import com.ai.image.gen.data.ImageRepositoryImpl
import com.ai.image.gen.data.SavedImagesRepositoryImpl
import com.ai.image.gen.data.local.AppDatabase
import com.ai.image.gen.data.worker.ImageEditWorker
import com.ai.image.gen.domain.GenerateImageUseCase
import com.ai.image.gen.domain.ImageRepository
import com.ai.image.gen.domain.SavedImagesRepository
import com.ai.image.gen.presentation.i2i.ImageEditViewModel
import com.ai.image.gen.presentation.queue.QueueViewModel
import com.ai.image.gen.presentation.saved.SavedImagesViewModel
import com.ai.image.gen.presentation.t2i.ImageViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    // 1. Network Clients
    single {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // Image gen takes time
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://router.huggingface.co/")
            .client(get())
            // Added Gson factory just in case we need to parse JSON errors later,
            // even though we handle the image body manually.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(HuggingFaceApi::class.java)
    }

    single { WorkManager.getInstance(androidContext()) }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "ai_image_studio_db"
        )
            .fallbackToDestructiveMigration(true) // For demo purposes, wipe DB if schema changes
            .build()
    }

    // 2. DAO (Scoped to Database)
    single { get<AppDatabase>().requestDao() }

    // 2. Repository (Singleton)
    // using singleOf(::Impl) { bind<Interface>() } for strict typing
    singleOf(::ImageRepositoryImpl) { bind<ImageRepository>() }
    singleOf(::SavedImagesRepositoryImpl) { bind<SavedImagesRepository>() }

    // 3. Use Case (Factory - created every time it's injected, usually lightweight)
    factoryOf(::GenerateImageUseCase)

    workerOf(::ImageEditWorker)

    // 4. ViewModel
    viewModelOf(::ImageViewModel)
    viewModelOf(::ImageEditViewModel)
    viewModelOf(::SavedImagesViewModel)
    viewModelOf(::QueueViewModel)
}