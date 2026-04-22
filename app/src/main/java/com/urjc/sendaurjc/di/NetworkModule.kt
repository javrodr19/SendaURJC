package com.urjc.sendaurjc.di

import com.urjc.sendaurjc.BuildConfig
import com.urjc.sendaurjc.data.remote.api.LumenSmartApi
import com.urjc.sendaurjc.data.remote.api.UrjcApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            })
            .build()

    @Provides
    @Singleton
    @Named("lumensmart")
    fun provideLumenSmartRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.LUMENSMART_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("urjc")
    fun provideUrjcRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.URJC_SSO_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideLumenSmartApi(@Named("lumensmart") retrofit: Retrofit): LumenSmartApi =
        retrofit.create(LumenSmartApi::class.java)

    @Provides
    @Singleton
    fun provideUrjcApi(@Named("urjc") retrofit: Retrofit): UrjcApi =
        retrofit.create(UrjcApi::class.java)
}
