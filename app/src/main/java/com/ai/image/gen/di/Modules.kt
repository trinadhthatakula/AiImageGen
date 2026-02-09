package com.ai.image.gen.di

import com.ai.image.gen.BuildConfig
import com.ai.image.gen.data.HuggingFaceApi
import com.ai.image.gen.data.ImageRepositoryImpl
import com.ai.image.gen.domain.GenerateImageUseCase
import com.ai.image.gen.domain.ImageRepository
import com.ai.image.gen.presentation.ImageViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
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

    // 2. Repository (Singleton)
    // using singleOf(::Impl) { bind<Interface>() } for strict typing
    singleOf(::ImageRepositoryImpl) { bind<ImageRepository>() }

    // 3. Use Case (Factory - created every time it's injected, usually lightweight)
    factoryOf(::GenerateImageUseCase)

    // 4. ViewModel
    viewModel { ImageViewModel(get()) }
}