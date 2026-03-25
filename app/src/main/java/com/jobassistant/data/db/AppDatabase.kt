package com.jobassistant.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jobassistant.data.db.dao.CareerInsightsDao
import com.jobassistant.data.db.dao.JobApplicationDao
import com.jobassistant.data.db.entity.CareerInsightsEntity
import com.jobassistant.data.db.entity.JobApplicationEntity
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [JobApplicationEntity::class, CareerInsightsEntity::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jobApplicationDao(): JobApplicationDao
    abstract fun careerInsightsDao(): CareerInsightsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE job_applications ADD COLUMN jobDescription TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Rename old enum values to new names
                database.execSQL("UPDATE job_applications SET status = 'INTERESTED' WHERE status = 'SAVED'")
                database.execSQL("UPDATE job_applications SET status = 'OFFER' WHERE status = 'OFFERED'")
                // APPLIED, INTERVIEWING, REJECTED are unchanged — no UPDATE needed
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE job_applications ADD COLUMN analysisDate INTEGER DEFAULT NULL"
                )
            }
        }

        fun create(context: Context, passphrase: String, converters: Converters): AppDatabase {
            val factory = SupportFactory(passphrase.toByteArray())
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "jobassistant.db"
            )
                .openHelperFactory(factory)
                .addTypeConverter(converters)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
        }
    }
}
