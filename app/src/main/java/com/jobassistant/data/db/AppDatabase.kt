package com.jobassistant.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jobassistant.data.db.dao.CareerInsightsDao
import com.jobassistant.data.db.dao.JobApplicationDao
import com.jobassistant.data.db.entity.CareerInsightsEntity
import com.jobassistant.data.db.entity.JobApplicationEntity
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [JobApplicationEntity::class, CareerInsightsEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jobApplicationDao(): JobApplicationDao
    abstract fun careerInsightsDao(): CareerInsightsDao

    companion object {
        fun create(context: Context, passphrase: String, converters: Converters): AppDatabase {
            val factory = SupportFactory(passphrase.toByteArray())
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "jobassistant.db"
            )
                .openHelperFactory(factory)
                .addTypeConverter(converters)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
