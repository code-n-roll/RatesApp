package com.karanchuk.ratesapp.di

import android.app.Application
import com.google.gson.Gson
import com.karanchuk.ratesapp.BuildConfig
import com.karanchuk.ratesapp.R
import com.karanchuk.ratesapp.data.api.RevolutApi
import com.karanchuk.ratesapp.data.Currencies
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import javax.inject.Singleton

@Module
class AppModule {

    companion object {
        private const val REVOLUT_BASE_URL = "https://revolut.duckdns.org/"
    }

    @Provides
    @Singleton
    fun provideRevolutApi(): RevolutApi {
        val interceptor = HttpLoggingInterceptor()
            .setLevel(
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            )
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(REVOLUT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient)
            .build()
            .create(RevolutApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideCurrencies(gson: Gson, app: Application): Currencies {
        val hashMapType = HashMap<String, String>()::class.java
        val codeToNameJson = app.resources.openRawResource(R.raw.currency_code_to_name)
            .bufferedReader()
            .use { it.readText() }
        val codeToName = gson.fromJson(codeToNameJson, hashMapType)

        val codeToFlagImageJson = app.resources.openRawResource(R.raw.currency_code_to_flag_image)
            .bufferedReader()
            .use { it.readText() }
        val codeToFlagImage = gson.fromJson(codeToFlagImageJson, hashMapType)

        return Currencies(codeToName, codeToFlagImage)
    }
}