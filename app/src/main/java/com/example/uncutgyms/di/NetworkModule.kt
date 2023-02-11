package com.example.uncutgyms.di

import com.example.uncutgyms.networking.DirectionsWebservice
import com.example.uncutgyms.networking.GymsWebservice
import com.example.uncutgyms.ui.main.util.JSONObjectAdapter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun ioCoroutineScope() = CoroutineScope(Dispatchers.IO)

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Singleton
    @Provides
    fun providesMoshi(): Moshi = Moshi.Builder()
        .add(JSONObjectAdapter())
        .build()

    @Provides
    fun provideRetrofit(
        moshi: Moshi,
        loggingInterceptor: HttpLoggingInterceptor,
    ): Retrofit {
        val builder = OkHttpClient.Builder()
        builder.callTimeout(30, TimeUnit.SECONDS)
        builder.retryOnConnectionFailure(true)
        builder.addInterceptor(BasicInterceptor())
        builder.addNetworkInterceptor(loggingInterceptor)
        val httpClient = builder.build()

        return Retrofit.Builder()
            .baseUrl("https://api.yelp.com/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun providesGymsWebservice(retrofit: Retrofit): GymsWebservice =
        retrofit.create(GymsWebservice::class.java)

    @Provides
    @Singleton
    fun provideMapWebService(retrofit: Retrofit): DirectionsWebservice =
        retrofit.create(DirectionsWebservice::class.java)

    // Uses Interceptor
    class BasicInterceptor() : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val builder = request.newBuilder().header(
                "Authorization",
                "Bearer jzt8FAzNstW4xnZssUtK0ioklHMohUkYYnfzKFxgL3AzuA9-3WgWNo3PwI" +
                        "uvywFxEbMusAqiYPZz4_A5Ttfm5ru1g86q229pcW7D1Qhcp52cYF4IyAGCQ6GLBuviY3Yx"
            )
            return chain.proceed(builder.build())
        }
    }
}
