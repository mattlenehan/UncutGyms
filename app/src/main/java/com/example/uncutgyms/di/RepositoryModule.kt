package com.example.uncutgyms.di

import android.content.Context
import com.example.uncutgyms.networking.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideGymRepository(
        gymsWebservice: GymsWebservice,
        retrofit: Retrofit,
    ): GymsRepository {
        return GymsRepositoryImpl(
            gymsWebservice = gymsWebservice,
            retrofit = retrofit,
        )
    }

    @Singleton
    @Provides
    fun provideLocationRepository(
        directionsWebservice: DirectionsWebservice,
        retrofit: Retrofit,
        @ApplicationContext appContext: Context,
    ): LocationRepository = LocationRepositoryImpl(
        directionsWebservice = directionsWebservice,
        retrofit = retrofit,
        context = appContext,
    )
}
