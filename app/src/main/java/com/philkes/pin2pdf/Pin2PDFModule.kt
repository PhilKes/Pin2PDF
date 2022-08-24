package com.philkes.pin2pdf

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.philkes.pin2pdf.api.TaskRunner
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
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

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

    class Settings(val context: Context, val dbService: DBService) {

        suspend fun resetUser(activity: Activity?) {
            val prefKey = context.resources.getString(R.string.app_name)
            val sharedPref = context.getSharedPreferences(prefKey, Context.MODE_PRIVATE)
            val preferenceEdit = sharedPref.edit()
            preferenceEdit.putString(context.resources.getString(R.string.key_user_name), null)
            preferenceEdit.apply()
            // Clear Database + local PDF Files
            dbService.clearAll()
            context.filesDir.listFiles()?.forEach { it.deleteRecursively() }

            val i = context.packageManager
                .getLaunchIntentForPackage(context.packageName)
            i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity?.finish()
            context.startActivity(i)
        }
    }

    @Singleton
    @Provides
    fun settings(@ApplicationContext context: Context, dbService: DBService) =
        Settings(context, dbService)

}