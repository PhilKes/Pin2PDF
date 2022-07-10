package com.philkes.pin2pdf

import android.content.Context
import androidx.room.Room
import com.philkes.pin2pdf.storage.database.PinDao
import com.philkes.pin2pdf.storage.database.AppDatabase
import com.philkes.pin2pdf.storage.database.DBService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import  com.philkes.pin2pdf.api.pinterest.PinterestAPI
@Module
@InstallIn(SingletonComponent::class)
class Pin2PDFModule {

    @Singleton
    @Provides
    fun dbService(pinDao: PinDao) = DBService(pinDao)

    @Singleton
    @Provides
    fun pinDao(appDatabase: AppDatabase) = appDatabase.pinDao()

    @Singleton
    @Provides
    fun appDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "app_database"
    ).build()

    @Singleton
    @Provides
    fun pinterestApi(@ApplicationContext context: Context) = PinterestAPI(context)

}