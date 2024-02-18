package io.github.philkes.pin2pdf

import android.content.Context
import androidx.room.Room
import io.github.philkes.pin2pdf.api.TaskRunner
import io.github.philkes.pin2pdf.api.pinterest.PinterestAPI
import io.github.philkes.pin2pdf.storage.database.AppDatabase
import io.github.philkes.pin2pdf.storage.database.DBService
import io.github.philkes.pin2pdf.storage.database.PinDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

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
    ).addMigrations(AppDatabase.MIGRATION_2_3)
        .build()

    @Singleton
    @Provides
    @Inject
    fun pinterestApi(@ApplicationContext context: Context) = PinterestAPI(context)

    @Singleton
    @Provides
    fun executorService() = Executors.newFixedThreadPool(3)

    @Singleton
    @Provides
    fun taskRunner() = TaskRunner(Executors.newFixedThreadPool(3))

    @Singleton
    @Provides
    fun settings(@ApplicationContext context: Context, dbService: DBService, pinterestAPI: PinterestAPI) =
        Settings(context, dbService, pinterestAPI)

}